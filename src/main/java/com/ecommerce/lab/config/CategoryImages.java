package com.ecommerce.lab.config;

import java.util.HashMap;
import java.util.Map;

public class CategoryImages {

        public static Map<String, Map<String, String>> categoryImageUrls = new HashMap<>();
        public static Map<String, String> homeGardenImages = new HashMap<>();
        public static Map<String, String> sportsImages = new HashMap<>();
        public static Map<String, String> electronicsImages = new HashMap<>();
        public static Map<String, String> clothingImages = new HashMap<>();
        public static Map<String, String> ramadanImages = new HashMap<>();

        // Home & Garden Images
        public static void setHomeGardenImages() {

                homeGardenImages.put("Coffee Maker",
                                "https://images.pexels.com/photos/302899/pexels-photo-302899.jpeg");
                homeGardenImages.put("Blender", "https://images.pexels.com/photos/3094224/pexels-photo-3094224.jpeg");
                homeGardenImages.put("Bed Sheets Set",
                                "https://images.pexels.com/photos/6585757/pexels-photo-6585757.jpeg");
                homeGardenImages.put("Throw Pillows",
                                "https://images.pexels.com/photos/1248583/pexels-photo-1248583.jpeg");
                homeGardenImages.put("Garden Tool Set",
                                "https://images.pexels.com/photos/1301856/pexels-photo-1301856.jpeg");
                homeGardenImages.put("Indoor Plant",
                                "https://images.pexels.com/photos/3076899/pexels-photo-3076899.jpeg");
                homeGardenImages.put("Desk Lamp", "https://images.pexels.com/photos/1112598/pexels-photo-1112598.jpeg");
                homeGardenImages.put("Bath Towels",
                                "https://images.pexels.com/photos/4210339/pexels-photo-4210339.jpeg");
                homeGardenImages.put("Wall Clock", "https://images.pexels.com/photos/210528/pexels-photo-210528.jpeg");
                homeGardenImages.put("Kitchen Knife Set",
                                "https://images.pexels.com/photos/4226881/pexels-photo-4226881.jpeg");
                homeGardenImages.put("Curtains", "https://images.pexels.com/photos/6492390/pexels-photo-6492390.jpeg");
                homeGardenImages.put("Vacuum Cleaner", null);
                categoryImageUrls.put("Home & Garden", homeGardenImages);

        }

        // Sports Images
        public static void setSportsImages() {

                sportsImages.put("Yoga Mat", "https://images.pexels.com/photos/3759657/pexels-photo-3759657.jpeg");
                sportsImages.put("Dumbbell Set 10kg",
                                "https://images.pexels.com/photos/949126/pexels-photo-949126.jpeg");
                sportsImages.put("Basketball", "https://images.pexels.com/photos/1752757/pexels-photo-1752757.jpeg");
                sportsImages.put("Tennis Racket", "https://images.pexels.com/photos/1432039/pexels-photo-1432039.jpeg");
                sportsImages.put("Running Shoes", "https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg");
                sportsImages.put("Water Bottle", "https://images.pexels.com/photos/1188649/pexels-photo-1188649.jpeg");
                sportsImages.put("Fitness Tracker", "https://images.pexels.com/photos/437037/pexels-photo-437037.jpeg");
                sportsImages.put("Jump Rope", "https://images.pexels.com/photos/4046718/pexels-photo-4046718.jpeg");
                sportsImages.put("Resistance Bands",
                                "https://images.pexels.com/photos/4397833/pexels-photo-4397833.jpeg");
                sportsImages.put("Soccer Ball", null);
                sportsImages.put("Gym Gloves", "https://images.pexels.com/photos/3120003/pexels-photo-3120003.jpeg");
                sportsImages.put("Sports Bag", "https://images.pexels.com/photos/1105666/pexels-photo-1105666.jpeg");
                categoryImageUrls.put("Sports", sportsImages);
        }

        // Books Images
        public static Map<String, String> booksImages = new HashMap<>();

        public static void setBooksImages() {

                booksImages.put("The Great Novel",
                                "https://images.pexels.com/photos/2312368/pexels-photo-2312368.jpeg");
                booksImages.put("Science Fiction Trilogy",
                                "https://images.pexels.com/photos/2312368/pexels-photo-2312368.jpeg");
                booksImages.put("Cookbook", "https://images.pexels.com/photos/262913/pexels-photo-262913.jpeg");
                booksImages.put("Self-Help Guide",
                                "https://images.pexels.com/photos/2312368/pexels-photo-2312368.jpeg");
                booksImages.put("History of Time",
                                "https://images.pexels.com/photos/2312368/pexels-photo-2312368.jpeg");
                booksImages.put("Children's Storybook",
                                "https://images.pexels.com/photos/2312368/pexels-photo-2312368.jpeg");
                booksImages.put("Poetry Collection",
                                "https://images.pexels.com/photos/2312368/pexels-photo-2312368.jpeg");
                booksImages.put("Biography", "https://images.pexels.com/photos/2312368/pexels-photo-2312368.jpeg");
                booksImages.put("Travel Guide", "https://images.pexels.com/photos/385997/pexels-photo-385997.jpeg");
                booksImages.put("Language Learning",
                                "https://images.pexels.com/photos/2312368/pexels-photo-2312368.jpeg");
                booksImages.put("Art Book", "https://images.pexels.com/photos/298660/pexels-photo-298660.jpeg");
                booksImages.put("Mystery Novel", "https://images.pexels.com/photos/2312368/pexels-photo-2312368.jpeg");
                categoryImageUrls.put("Books", booksImages);
        }

        // Electronics Images
        public static void setElectronicsImages() {

                electronicsImages.put("Wireless Headphones",
                                "https://images.pexels.com/photos/3394651/pexels-photo-3394651.jpeg");
                electronicsImages.put("4K Ultra HD TV",
                                "https://images.pexels.com/photos/5721865/pexels-photo-5721865.jpeg");
                electronicsImages.put("Smartphone", "https://images.pexels.com/photos/607812/pexels-photo-607812.jpeg");
                electronicsImages.put("Laptop Pro", "https://images.pexels.com/photos/18105/pexels-photo.jpg");
                electronicsImages.put("Tablet 10-inch",
                                "https://images.pexels.com/photos/1334597/pexels-photo-1334597.jpeg");
                electronicsImages.put("Bluetooth Speaker",
                                "https://images.pexels.com/photos/1279107/pexels-photo-1279107.jpeg");
                electronicsImages.put("Smart Watch",
                                "https://images.pexels.com/photos/267394/pexels-photo-267394.jpeg");
                electronicsImages.put("Gaming Console",
                                "https://images.pexels.com/photos/4219883/pexels-photo-4219883.jpeg");
                electronicsImages.put("Digital Camera",
                                "https://images.pexels.com/photos/90946/pexels-photo-90946.jpeg");
                electronicsImages.put("Wireless Mouse",
                                "https://images.pexels.com/photos/2115256/pexels-photo-2115256.jpeg");
                electronicsImages.put("Mechanical Keyboard",
                                "https://images.pexels.com/photos/1772123/pexels-photo-1772123.jpeg");
                electronicsImages.put("External SSD",
                                "https://images.pexels.com/photos/4443494/pexels-photo-4443494.jpeg");
                categoryImageUrls.put("Electronics", electronicsImages);
        }

        // Clothing Images

        public static void setClothingImages() {

                clothingImages.put("Men's Cotton T-Shirt",
                                "https://images.pexels.com/photos/4066288/pexels-photo-4066288.jpeg");
                clothingImages.put("Women's Jeans",
                                "https://images.pexels.com/photos/1082528/pexels-photo-1082528.jpeg");
                clothingImages.put("Hoodie Sweatshirt",
                                "https://images.pexels.com/photos/1183266/pexels-photo-1183266.jpeg");
                clothingImages.put("Running Shorts", null);
                clothingImages.put("Leather Jacket",
                                "https://images.pexels.com/photos/1124468/pexels-photo-1124468.jpeg");
                clothingImages.put("Summer Dress", "https://images.pexels.com/photos/985635/pexels-photo-985635.jpeg");
                clothingImages.put("Wool Sweater", "https://images.pexels.com/photos/459486/pexels-photo-459486.jpeg");
                clothingImages.put("Sports Bra", "https://images.pexels.com/photos/4498551/pexels-photo-4498551.jpeg");
                clothingImages.put("Yoga Pants", "https://images.pexels.com/photos/4661214/pexels-photo-4661214.jpeg");
                clothingImages.put("Denim Jacket",
                                "https://images.pexels.com/photos/11516091/pexels-photo-11516091.jpeg");
                clothingImages.put("Polo Shirt", "https://images.pexels.com/photos/1232459/pexels-photo-1232459.jpeg");
                clothingImages.put("Winter Coat", "https://images.pexels.com/photos/843266/pexels-photo-843266.jpeg");
                categoryImageUrls.put("Clothing", clothingImages);
        }

        // Ramadan Images
        public static void setRamadanImages() {

                ramadanImages.put("Prayer Mat", "https://images.pexels.com/photos/8164381/pexels-photo-8164381.jpeg");
                ramadanImages.put("Holy Quran", "https://images.pexels.com/photos/8164742/pexels-photo-8164742.jpeg");
                ramadanImages.put("Ramadan Lantern",
                                "https://images.pexels.com/photos/2233416/pexels-photo-2233416.jpeg");
                ramadanImages.put("Misbaha Beads",
                                "https://images.pexels.com/photos/8164375/pexels-photo-8164375.jpeg");
                ramadanImages.put("Premium Dates",
                                "https://images.pexels.com/photos/2291592/pexels-photo-2291592.jpeg");
                ramadanImages.put("Incense Burner",
                                "https://images.pexels.com/photos/6634653/pexels-photo-6634653.jpeg");
                ramadanImages.put("Abaya", "https://images.pexels.com/photos/21274065/pexels-photo-21274065.jpeg");
                ramadanImages.put("Islamic Wall Art",
                                "https://images.pexels.com/photos/8164567/pexels-photo-8164567.jpeg");
                ramadanImages.put("Sebha Tasbih",
                                "https://images.pexels.com/photos/7249183/pexels-photo-7249183.jpeg");
                ramadanImages.put("Gift Hamper", "https://images.pexels.com/photos/5742133/pexels-photo-5742133.jpeg");
                ramadanImages.put("Attar Oil", "https://images.pexels.com/photos/6707140/pexels-photo-6707140.jpeg");
                ramadanImages.put("Traditional Kettle",
                                "https://images.pexels.com/photos/221436/pexels-photo-221436.jpeg");

                categoryImageUrls.put("Ramadan", ramadanImages);
        }

        public static String getFallbackImageUrl(String category) {
                switch (category) {
                        case "Electronics":
                                return "https://images.pexels.com/photos/577769/pexels-photo-577769.jpeg";
                        case "Clothing":
                                return "https://images.pexels.com/photos/2983464/pexels-photo-2983464.jpeg";
                        case "Home & Garden":
                                return "https://images.pexels.com/photos/276724/pexels-photo-276724.jpeg";
                        case "Books":
                                return "https://images.pexels.com/photos/159711/books-bookstore-book-reading-159711.jpeg";
                        case "Sports":
                                return "https://images.pexels.com/photos/248547/pexels-photo-248547.jpeg";
                        default:
                                return "https://images.pexels.com/photos/90946/pexels-photo-90946.jpeg";
                }
        }
}
