# Android responsive layout notes

This note covers the Smart Netease Android app on phones and larger Android windows.

## Layout principles

- Measure the available app window instead of assuming a fixed phone width.
- Keep page gutters consistent and let content use the available width on phones.
- On large windows, use an intentional content measure and multi-column layouts where the screen benefits from them.
- Include system bar insets in the available layout and keep player controls clear of system navigation.
- Use the shared `HeightClass` and existing player compact/tiny modes for constrained vertical space.
- Keep image aspect ratios stable so asynchronous loading does not shift surrounding content.

## Implementation locations

- `app/src/main/java/com/smart/neteast/ui/ResponsiveContent.kt` — responsive content width and gutters.
- `app/src/main/java/com/smart/neteast/ui/HeightClass.kt` — vertical layout classification.
- `app/src/main/java/com/smart/neteast/ui/player/PlayerCard.kt` — full-screen player geometry and insets.
- `app/src/main/java/com/smart/neteast/ui/player/FullPlayerControls.kt` — compact player control arrangements.

When changing these areas, validate at least one compact phone and one larger Android window in portrait and landscape. Record the device size and whether the check was visual, built, or interaction-tested.
