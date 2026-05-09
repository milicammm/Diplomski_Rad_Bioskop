import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-about-page',
  template: `
    <section class="about-page">
      <header class="hero">
        <div class="hero__copy">
          <p class="hero__eyebrow">About CineTicket</p>
          <h1>Premium cinema booking, built for people who value speed and simplicity.</h1>
          <p>
            CineTicket helps movie lovers discover screenings faster, reserve the best seats without friction, and enjoy a
            smoother journey from browsing to checkout.
          </p>
        </div>

        <div class="hero__gallery" aria-label="Cinema atmosphere preview">
          <img
            src="https://www.ediskont.rs/uploads/news/images/15-zanimljivosti-o-kokicama-121.webp"
            alt="Popcorn in a cinema bucket"
          />
          <img
            src="https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=900&q=80"
            alt="Movie night audience and lights"
          />
          <img
            src="https://tob.co.rs/wp-content/webp-express/webp-images/doc-root/wp-content/uploads/2019/09/sedista-bioskop-sala.jpg.webp"
            alt="Modern cinema theater with red seats"
          />
        </div>
      </header>

      <section class="grid">
        <article class="panel">
          <h2>What CineTicket gives you</h2>
          <ul>
            <li>A polished movie browsing experience with clear screening options</li>
            <li>Fast seat selection with live availability in mind</li>
            <li>Secure user accounts and a clean checkout flow</li>
            <li>Dedicated admin tools for day-to-day cinema operations</li>
          </ul>
        </article>

        <article class="panel">
          <h2>Our approach</h2>
          <ul>
            <li>Keep the experience quick, intuitive, and visually calm</li>
            <li>Reduce friction between discovery, reservation, and payment</li>
            <li>Make admin work easier with one dashboard for all key actions</li>
            <li>Present cinema content in a clean, premium interface</li>
          </ul>
        </article>

        <article class="panel">
          <h2>Why customers remember it</h2>
          <ul>
            <li>It feels fast, consistent, and easy to trust</li>
            <li>It gives cinema teams control without overwhelming them</li>
            <li>It scales better when there are many movies, screenings, and users</li>
            <li>It is made to feel premium on both desktop and mobile</li>
          </ul>
        </article>

        <article class="panel panel--wide panel--story">
          <div class="story-layout">
            <div>
              <p class="panel__eyebrow panel__eyebrow--soft">Our story</p>
              <h2>Born from the idea that cinema booking should feel effortless.</h2>
            </div>
            <p>
              CineTicket was created to bring clarity into the booking process: fewer clicks, better structure, and a
              more elegant experience for everyone who visits the cinema or manages it.
            </p>
          </div>
        </article>

        <article class="panel panel--wide panel--contact">
          <div class="contact-grid">
            <div>
              <p class="panel__eyebrow panel__eyebrow--soft">Contact</p>
              <h2>Need help or want to talk about a cinema partnership?</h2>
              <p>
                Our team is available for product questions, cinema onboarding, and support requests.
              </p>
            </div>

            <div class="contact-card">
              <span>Phone</span>
              <strong>+387 61 234 567</strong>
              <span>Email</span>
              <strong>support@cineticket.com</strong>
              <span>Office</span>
              <strong>Novi Sad, Srbija</strong>
            </div>
          </div>
        </article>
      </section>
    </section>
  `,
  styles: [`
    :host {
      display: block;
    }

    .about-page {
      display: grid;
      gap: 1rem;
    }

    .hero {
      display: grid;
      grid-template-columns: minmax(0, 1.15fr) minmax(280px, 0.85fr);
      gap: 1.2rem;
      border-radius: 22px;
      padding: clamp(1.2rem, 3vw, 2rem);
      background:
        radial-gradient(circle at top right, rgba(14, 165, 233, 0.18), transparent 26%),
        linear-gradient(140deg, rgba(15, 23, 42, 0.84), rgba(30, 41, 59, 0.9));
      border: 1px solid rgba(255, 255, 255, 0.08);
    }

    .hero__eyebrow {
      margin: 0;
      text-transform: uppercase;
      letter-spacing: 0.16em;
      color: #93c5fd;
      font-size: 0.72rem;
    }

    .hero h1 {
      margin: 0.55rem 0;
      color: #f8fafc;
      max-width: 18ch;
    }

    .hero p {
      margin: 0;
      color: #cbd5e1;
      max-width: 62ch;
    }

    .hero__gallery {
      display: grid;
      grid-template-columns: 1.05fr 0.85fr;
      grid-template-rows: 1fr 1fr;
      gap: 0.7rem;
      min-height: 300px;
    }

    .hero__gallery img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
      border-radius: 18px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 14px 30px rgba(2, 6, 23, 0.32);
    }

    .hero__gallery img:first-child {
      grid-row: 1 / -1;
      min-height: 280px;
    }

    .grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 1rem;
    }

    .panel {
      border-radius: 18px;
      border: 1px solid rgba(255, 255, 255, 0.08);
      background: rgba(15, 23, 42, 0.55);
      padding: 1rem;
    }

    .panel h2 {
      margin: 0 0 0.8rem;
      color: #f8fafc;
    }

    .panel ul {
      margin: 0;
      padding-left: 1.2rem;
      color: #cbd5e1;
      display: grid;
      gap: 0.55rem;
    }

    .panel--wide {
      grid-column: 1 / -1;
    }

    .panel--story {
      background:
        radial-gradient(circle at top right, rgba(56, 189, 248, 0.12), transparent 35%),
        linear-gradient(135deg, rgba(15, 23, 42, 0.62), rgba(30, 41, 59, 0.86));
    }

    .panel--contact {
      background:
        linear-gradient(135deg, rgba(59, 130, 246, 0.16), rgba(15, 23, 42, 0.65)),
        rgba(15, 23, 42, 0.55);
    }

    .panel__eyebrow--soft {
      color: #93c5fd;
      opacity: 0.9;
    }

    .story-layout,
    .contact-grid {
      display: grid;
      grid-template-columns: 1.2fr 0.9fr;
      gap: 1rem;
      align-items: start;
    }

    .story-layout p,
    .contact-grid p {
      margin: 0;
      color: #cbd5e1;
      line-height: 1.7;
    }

    .contact-card {
      display: grid;
      gap: 0.35rem;
      padding: 1rem;
      border-radius: 16px;
      border: 1px solid rgba(147, 197, 253, 0.2);
      background: rgba(15, 23, 42, 0.7);
    }

    .contact-card span {
      color: #93c5fd;
      font-size: 0.78rem;
      text-transform: uppercase;
      letter-spacing: 0.12em;
      margin-top: 0.4rem;
    }

    .contact-card strong {
      color: #f8fafc;
      font-size: 1rem;
    }

    .contact-card span:first-child {
      margin-top: 0;
    }

    @media (max-width: 900px) {
      .hero {
        grid-template-columns: 1fr;
      }

      .hero__gallery {
        grid-template-columns: 1fr 1fr;
        min-height: 240px;
      }

      .hero__gallery img:first-child {
        min-height: 240px;
      }

      .grid {
        grid-template-columns: 1fr;
      }

      .panel--wide {
        grid-column: auto;
      }

      .story-layout,
      .contact-grid {
        grid-template-columns: 1fr;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AboutPageComponent {}