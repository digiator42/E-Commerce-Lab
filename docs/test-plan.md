# Test Plan

## Scope
| Module | Manual | Automated (JUnit) | Automated (Selenium) |
|---|---|---|---|
| Authentication | ✅ | ✅ | 🔲 in-progress |
| 2FA | ✅ | ✅ | 🔲 in-progress |
| Products | ✅ | ✅ | 🔲 in-progress |
| Orders | ✅ | ✅ | 🔲 in-progress |
| Cart | ✅ | ✅ | 🔲 in-progress |

## Test Levels
| Level | Tool | Who |
|---|---|---|
| Unit / Integration | JUnit 5, MockMvc | Developer |
| Manual | Test cases in /manual | Tester |
| E2E Automation | Selenium | Tester |

## Exit Criteria
- [ ] All framework tests pass (>95%)
- [ ] All manual test cases executed
- [ ] No critical/high bugs open
- [ ] Traceability matrix covered

