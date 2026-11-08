# Full-player layout on compact Android windows

Status: implemented in `ui/player/PlayerCard.kt` and `ui/player/FullPlayerControls.kt`.

The player derives cover size and vertical placement from the measured content area, status/navigation insets, title area and controls area. Compact height classes reduce lyric typography and control spacing so the seek bar and transport controls remain visible without clipping.

When revisiting this layout:

1. Keep cover sizing based on measured available space rather than a fixed screen-width assumption.
2. Keep the title, cover and transport controls out of system bar regions.
3. Preserve the shared cover animation between full player, lyric header and mini player.
4. Check expanded, lyric, queue and collapsed states on a compact phone and a larger Android window.
