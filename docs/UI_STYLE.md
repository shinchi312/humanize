# UI Style Blueprint (Reference: testimonial-v2 feel)

Design direction based on your reference:
clean, premium, soft-neutral cards, high whitespace, smooth motion, rounded corners, light/dark support.

## 1) Core look

- Background: light neutral base with subtle gradients/noise, not flat white.
- Card language: large radius (`24px`), soft border, low-elevation shadow.
- Content density: breathing space first, minimal clutter.
- Tone: editorial + product (calm, premium, trustworthy).

## 2) Typography

- Heading font: `Sora` (bold, high contrast for hero/section titles).
- Body font: `Manrope` (clean long-form readability).
- Scale:
1. Hero: `56/64` desktop, `36/44` mobile.
2. Section title: `40/48` desktop, `30/38` mobile.
3. Body: `18/30` for feature copy, `16/26` for cards.

## 3) Color tokens

- `--bg`: `#f7f7f5`
- `--panel`: `#ffffff`
- `--panel-dark`: `#141414`
- `--text`: `#111113`
- `--muted`: `#6b7280`
- `--border`: `#e5e7eb`
- `--accent`: `#0ea5a4` (teal, used sparingly)
- `--success`: `#15803d`
- `--warning`: `#c2410c`

No purple-first styling.

## 4) Motion language

- Scroll reveals: `fade + y-translate` (200-350ms).
- Card hover: subtle lift (`-6px`) + shadow deepening.
- Carousel/column loops: slow linear vertical movement for social proof blocks.
- Page transitions: short (`180-220ms`) with no heavy blur.

## 5) Component style rules

### Navigation
- Slim top bar with soft border, sticky, translucent background.
- Primary CTA as rounded pill button.

### Library cards
- Rounded rectangles with cover, title, author, progress ring.
- Hover expands quick actions: `Read`, `Continue`, `Set Reminder`.

### Reader view
- Distraction-free center column.
- Page container with paper texture and clear margin controls.
- Progress rail fixed at bottom for quick jump.

### Upload flow
- Stepper card:
1. Upload
2. Processing
3. Metadata review
4. Ready to read
- Real-time processing state from Kafka-backed events.

### Notification settings
- Preference card with quiet-hours slider and spoiler intensity selector.
- Email-only channel shown now; push/SMS placeholders disabled.

## 6) Screen structure (MVP)

1. Auth page (Google sign-in only, minimal split layout).
2. Library dashboard (hero + reading stats + cards + recommendations).
3. Book detail/reader page.
4. Upload and processing status page.
5. Notification preferences page.

## 7) Accessibility baseline

- WCAG AA contrast minimum.
- Keyboard focus ring visible on all interactive elements.
- Reduced motion mode support.
- Semantic landmarks for reader mode and long content sections.

## 8) Implementation notes for frontend

- React + Tailwind + Framer Motion.
- Use CSS variables for all tokens in one `theme.css`.
- Build reusable primitives: `Card`, `Badge`, `Metric`, `SectionHeader`, `SpoilerPreview`.
- Keep desktop + mobile parity from first pass.
