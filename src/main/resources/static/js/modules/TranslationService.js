export class TranslationService {
    static instance = null;

    constructor() {
        this.currentLang = localStorage.getItem('language') || 'en';
        this.supportedLanguages = ['en', 'de', 'ar'];
        this.cache = {};
        this.rtlLanguages = ['ar'];
        this.batchSize = 50; // Max texts per batch (Google limit)
    }

    static getInstance() {
        if (!TranslationService.instance) {
            TranslationService.instance = new TranslationService();
        }
        return TranslationService.instance;
    }

    async init() {
        if (this.currentLang !== 'en') {
            await this.translatePage(this.currentLang);
        }
        this.applyLanguageDirection();
        return this.currentLang;
    }

    applyLanguageDirection() {
        const html = document.documentElement;
        if (this.rtlLanguages.includes(this.currentLang)) {
            html.setAttribute('dir', 'rtl');
            html.classList.add('rtl');
        } else {
            html.setAttribute('dir', 'ltr');
            html.classList.remove('rtl');
        }
    }

    extractTranslatableContent(container) {
        const texts = new Set();
        const elements = [];
        const placeholders = [];
        const alts = [];
        const titles = [];

        const cleanText = (text) => text?.trim().replace(/\s+/g, ' ').substring(0, 1000);

        // Walk through all text nodes
        const walker = document.createTreeWalker(
            container,
            NodeFilter.SHOW_TEXT,
            {
                acceptNode: (node) => {
                    if (!node.textContent.trim()) return NodeFilter.FILTER_REJECT;
                    const parent = node.parentElement;
                    if (parent?.tagName === 'SCRIPT' ||
                        parent?.tagName === 'STYLE' ||
                        parent?.tagName === 'CODE') {
                        return NodeFilter.FILTER_REJECT;
                    }
                    return NodeFilter.FILTER_ACCEPT;
                }
            }
        );

        while (walker.nextNode()) {
            const text = cleanText(walker.currentNode.textContent);
            if (text && !texts.has(text)) {
                texts.add(text);

                // Store original on parent element for easy reversion
                const parent = walker.currentNode.parentElement;
                if (parent && !parent.hasAttribute('data-original')) {
                    parent.setAttribute('data-original', parent.textContent);
                }

                elements.push({
                    node: walker.currentNode,
                    original: text,
                    type: 'text'
                });
            }
        }

        // Collect placeholders
        container.querySelectorAll('input[placeholder], textarea[placeholder]').forEach(el => {
            const text = cleanText(el.placeholder);
            if (text && !el.hasAttribute('data-original-placeholder')) {
                el.setAttribute('data-original-placeholder', text);
                if (!texts.has(text)) {
                    texts.add(text);
                    placeholders.push({
                        element: el,
                        original: text,
                        type: 'placeholder'
                    });
                }
            }
        });

        // Collect alt texts
        container.querySelectorAll('img[alt]').forEach(el => {
            const text = cleanText(el.alt);
            if (text && !el.hasAttribute('data-original-alt') && text !== '') {
                el.setAttribute('data-original-alt', text);
                if (!texts.has(text)) {
                    texts.add(text);
                    alts.push({
                        element: el,
                        original: text,
                        type: 'alt'
                    });
                }
            }
        });

        // Collect titles
        container.querySelectorAll('[title]').forEach(el => {
            const text = cleanText(el.title);
            if (text && !el.hasAttribute('data-original-title') && text !== '') {
                el.setAttribute('data-original-title', text);
                if (!texts.has(text)) {
                    texts.add(text);
                    titles.push({
                        element: el,
                        original: text,
                        type: 'title'
                    });
                }
            }
        });

        return {
            texts: Array.from(texts),
            elements,
            placeholders,
            alts,
            titles
        };
    }
    async batchTranslate(texts, targetLang) {
        if (!texts.length || targetLang === 'en') {
            return texts.reduce((acc, text) => ({ ...acc, [text]: text }), {});
        }

        // Check cache first
        const uncached = [];
        const translations = {};

        texts.forEach(text => {
            const cacheKey = `${text}_${targetLang}`;
            if (this.cache[cacheKey]) {
                translations[text] = this.cache[cacheKey];
            } else {
                uncached.push(text);
            }
        });

        if (uncached.length === 0) {
            return translations;
        }

        // Split into batches
        const batches = [];
        for (let i = 0; i < uncached.length; i += this.batchSize) {
            batches.push(uncached.slice(i, i + this.batchSize));
        }

        if (uncached.length > 100) {
            this.showProgress(`Translating ${uncached.length} texts...`);
        }

        // Process each batch
        for (const batch of batches) {
            try {
                // Join texts with a unique separator
                const q = batch.join(' [SEP] ');
                const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=${targetLang}&dt=t&q=${encodeURIComponent(q)}`;

                const response = await fetch(url);
                const data = await response.json();

                // FIXED: Properly parse the Google Translate response
                // The response structure is: [[["translated","original",...,...], ...], ...]
                if (data && data[0]) {
                    // Get all translated segments
                    const translatedSegments = data[0];

                    // Reconstruct the full translated text
                    let fullTranslatedText = '';
                    for (let i = 0; i < translatedSegments.length; i++) {
                        if (translatedSegments[i] && translatedSegments[i][0]) {
                            fullTranslatedText += translatedSegments[i][0];
                        }
                    }

                    // Now split by [SEP] to get individual translations
                    // But note: Google might translate the separators too, so we need a better approach

                    // Better approach: Use the fact that Google returns segments in order
                    // and each original text maps to one translated segment

                    // If we have the same number of segments as batch items
                    if (translatedSegments.length === batch.length) {
                        // Direct mapping - each segment corresponds to one original text
                        batch.forEach((original, index) => {
                            const translated = translatedSegments[index]?.[0] || original;
                            const cacheKey = `${original}_${targetLang}`;
                            this.cache[cacheKey] = translated;
                            translations[original] = translated;
                        });
                    } else {
                        // Fallback: split by [SEP] but clean up
                        const translatedText = data[0].map(item => item[0]).join('');

                        // Try to split by [SEP] (but Google might translate it)
                        const possibleSeparators = ['[SEP]', 'SEP', '|', ','];
                        let splitTexts = [translatedText];

                        for (const sep of possibleSeparators) {
                            if (translatedText.includes(sep)) {
                                splitTexts = translatedText.split(sep).map(t => t.trim());
                                break;
                            }
                        }

                        // If we got the right number of texts
                        if (splitTexts.length === batch.length) {
                            batch.forEach((original, index) => {
                                const translated = splitTexts[index] || original;
                                const cacheKey = `${original}_${targetLang}`;
                                this.cache[cacheKey] = translated;
                                translations[original] = translated;
                            });
                        } else {
                            // Last resort: use the full text as one translation (not ideal)
                            console.warn('Batch translation mismatch, using fallback');
                            const fullTranslation = translatedText;
                            batch.forEach((original, index) => {
                                const cacheKey = `${original}_${targetLang}`;
                                this.cache[cacheKey] = fullTranslation;
                                translations[original] = fullTranslation;
                            });
                        }
                    }
                }
            } catch (error) {
                console.error('Batch translation failed:', error);
                batch.forEach(text => {
                    translations[text] = text;
                });
            }
        }

        if (uncached.length > 100) {
            this.hideProgress();
        }

        return translations;
    }

    async translatePage(targetLang) {
        if (targetLang === this.currentLang) return;

        const startTime = performance.now();
        const previousLang = this.currentLang;
        this.currentLang = targetLang;
        localStorage.setItem('language', targetLang);

        const loader = this.showLoader();

        try {
            // SPECIAL HANDLER FOR ENGLISH - Revert to original
            if (targetLang === 'en') {
                console.log('Reverting to English...');

                // Revert all text nodes with data-original
                document.querySelectorAll('[data-original]').forEach(element => {
                    element.textContent = element.getAttribute('data-original');
                });

                // Revert placeholders
                document.querySelectorAll('[data-original-placeholder]').forEach(element => {
                    element.placeholder = element.getAttribute('data-original-placeholder');
                });

                // Revert alt texts
                document.querySelectorAll('[data-original-alt]').forEach(element => {
                    element.alt = element.getAttribute('data-original-alt');
                });

                // Revert titles
                document.querySelectorAll('[data-original-title]').forEach(element => {
                    element.title = element.getAttribute('data-original-title');
                });

                // Also revert any elements that might have been translated directly
                // This handles cases where we might have missed storing original
                this.applyLanguageDirection();
                this.updateSwitcherButton();

                const endTime = performance.now();
                console.log(`✅ Reverted to English in ${((endTime - startTime) / 1000).toFixed(2)}s`);

                this.hideLoader(loader);
                return;
            }

            // COLLECT ALL CONTENT (same as before for non-English languages)
            const content = document.getElementById('content');
            const navbar = document.querySelector('nav');
            const footer = document.querySelector('footer');

            const allContent = [];
            const allElements = [];

            [content, navbar, footer].forEach(container => {
                if (container) {
                    const extracted = this.extractTranslatableContent(container);
                    allContent.push(...extracted.texts);
                    allElements.push({
                        container,
                        elements: extracted.elements,
                        placeholders: extracted.placeholders,
                        alts: extracted.alts,
                        titles: extracted.titles
                    });
                }
            });

            // Remove duplicates
            const uniqueTexts = [...new Set(allContent)];

            if (uniqueTexts.length > 0) {
                // Batch translate all unique texts
                const translations = await this.batchTranslate(uniqueTexts, targetLang);

                // Apply translations to all elements
                allElements.forEach(group => {
                    // Update text nodes
                    group.elements.forEach(item => {
                        if (translations[item.original]) {
                            // Store original if not already stored
                            if (!item.node.parentElement?.hasAttribute('data-original')) {
                                const parent = item.node.parentElement;
                                if (parent) {
                                    parent.setAttribute('data-original', parent.textContent);
                                }
                            }
                            item.node.textContent = item.node.textContent.replace(
                                item.original,
                                translations[item.original]
                            );
                        }
                    });

                    // Update placeholders
                    group.placeholders.forEach(item => {
                        if (translations[item.original]) {
                            item.element.placeholder = translations[item.original];
                        }
                    });

                    // Update alt texts
                    group.alts.forEach(item => {
                        if (translations[item.original]) {
                            item.element.alt = translations[item.original];
                        }
                    });

                    // Update titles
                    group.titles.forEach(item => {
                        if (translations[item.original]) {
                            item.element.title = translations[item.original];
                        }
                    });
                });
            }

            this.applyLanguageDirection();
            this.updateSwitcherButton();

            // const endTime = performance.time();
            // console.log(`✅ Translation completed in ${((endTime - startTime) / 1000).toFixed(2)}s for ${uniqueTexts.length} texts`);

        } catch (error) {
            console.error('Translation failed:', error);
            // Revert to previous language on error
            this.currentLang = previousLang;
            localStorage.setItem('language', previousLang);
        } finally {
            this.hideLoader(loader);
        }
    }

    async translateContainer(container, targetLang) {
        if (!container || targetLang === 'en') return;

        const extracted = this.extractTranslatableContent(container);

        if (extracted.texts.length > 0) {
            const translations = await this.batchTranslate(extracted.texts, targetLang);

            // Apply translations
            extracted.elements.forEach(item => {
                if (translations[item.original]) {
                    item.node.textContent = item.node.textContent.replace(
                        item.original,
                        translations[item.original]
                    );
                }
            });

            extracted.placeholders.forEach(item => {
                if (translations[item.original]) {
                    item.element.placeholder = translations[item.original];
                }
            });

            extracted.alts.forEach(item => {
                if (translations[item.original]) {
                    item.element.alt = translations[item.original];
                }
            });

            extracted.titles.forEach(item => {
                if (translations[item.original]) {
                    item.element.title = translations[item.original];
                }
            });
        }
    }

    showLoader() {
        const loader = document.createElement('div');
        loader.className = 'fixed top-20 right-4 bg-blue-600 text-white px-4 py-2 rounded-lg shadow-lg z-50 animate-pulse';
        loader.id = 'translation-loader';
        loader.innerHTML = '🌐 Translating...';
        document.body.appendChild(loader);
        return loader;
    }

    showProgress(message) {
        const progress = document.createElement('div');
        progress.className = 'fixed top-20 right-4 bg-indigo-600 text-white px-4 py-2 rounded-lg shadow-lg z-50';
        progress.id = 'translation-progress';
        progress.innerHTML = message;
        document.body.appendChild(progress);
    }

    hideProgress() {
        document.getElementById('translation-progress')?.remove();
    }

    hideLoader(loader) {
        if (loader) {
            loader.remove();
        }
        this.hideProgress();
    }

    updateSwitcherButton() {
        const button = document.getElementById('current-language-btn');
        if (button) {
            const langNames = {
                en: '🇬🇧 English',
                de: '🇩🇪 Deutsch',
                ar: '🇸🇦 العربية'
            };
            const span = button.querySelector('span');
            if (span) {
                span.textContent = langNames[this.currentLang] || '🌐 Language';
            }
        }
    }

    getCurrentLanguage() {
        return this.currentLang;
    }
}