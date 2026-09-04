---
key: override-a-bean-by-name-not-by-redefinition
tags: [backend, spring, testing]
---
# A @TestConfiguration bean with the same name is a BeanDefinitionOverrideException

Defining `clock()` in a test configuration when the app already defines `clock()`
fails at context startup. Enabling bean-definition overriding makes it pass and
hides real duplicate-bean bugs everywhere else.

**Do:** give the test bean a different method name and mark it `@Primary`.
See `BookingContractVerificationTest.FixedClock`.
