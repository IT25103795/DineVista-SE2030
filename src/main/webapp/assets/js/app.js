(() => {
    'use strict';

    const q = (selector, scope = document) => scope.querySelector(selector);
    const qa = (selector, scope = document) => Array.from(scope.querySelectorAll(selector));

    const THEME_STORAGE_KEY = 'dinevista-theme';
    const root = document.documentElement;
    const themeToggle = q('[data-theme-toggle]');
    const themeLabel = q('[data-theme-label]', themeToggle || document);
    const systemTheme = window.matchMedia('(prefers-color-scheme: dark)');

    const storedTheme = () => {
        try {
            const value = localStorage.getItem(THEME_STORAGE_KEY);
            return value === 'dark' || value === 'light' ? value : null;
        } catch (error) {
            return null;
        }
    };

    const setTheme = (theme, persist = true) => {
        const nextTheme = theme === 'dark' ? 'dark' : 'light';
        root.dataset.theme = nextTheme;
        root.style.colorScheme = nextTheme;

        const switchingTo = nextTheme === 'dark' ? 'light' : 'dark';
        const accessibleLabel = `Switch to ${switchingTo} mode`;
        if (themeToggle) {
            themeToggle.setAttribute('aria-label', accessibleLabel);
            themeToggle.setAttribute('title', accessibleLabel);
            themeToggle.setAttribute('aria-pressed', String(nextTheme === 'dark'));
        }
        if (themeLabel) themeLabel.textContent = `${titleCase(nextTheme)} mode active`;

        const themeColor = q('meta[name="theme-color"]');
        if (themeColor) themeColor.setAttribute('content', nextTheme === 'dark' ? '#0b0d10' : '#fffdf9');

        if (persist) {
            try {
                localStorage.setItem(THEME_STORAGE_KEY, nextTheme);
            } catch (error) {
                // Theme still applies for this session when storage is unavailable.
            }
        }

        window.dispatchEvent(new CustomEvent('dinevista:themechange', { detail: { theme: nextTheme } }));
    };

    setTheme(root.dataset.theme || storedTheme() || (systemTheme.matches ? 'dark' : 'light'), false);

    themeToggle?.addEventListener('click', () => {
        const nextTheme = root.dataset.theme === 'dark' ? 'light' : 'dark';
        setTheme(nextTheme);
    });

    const handleSystemThemeChange = event => {
        if (!storedTheme()) setTheme(event.matches ? 'dark' : 'light', false);
    };
    if (typeof systemTheme.addEventListener === 'function') {
        systemTheme.addEventListener('change', handleSystemThemeChange);
    } else if (typeof systemTheme.addListener === 'function') {
        systemTheme.addListener(handleSystemThemeChange);
    }

    const menuToggle = q('[data-menu-toggle]');
    const mainNav = q('[data-main-nav]');
    menuToggle?.addEventListener('click', () => {
        const open = mainNav?.classList.toggle('open');
        menuToggle.setAttribute('aria-expanded', String(Boolean(open)));
    });
    qa('[data-main-nav] a').forEach(link => link.addEventListener('click', () => {
        mainNav?.classList.remove('open');
        menuToggle?.setAttribute('aria-expanded', 'false');
    }));

    const today = new Date();
    const localToday = new Date(today.getTime() - today.getTimezoneOffset() * 60000)
        .toISOString().slice(0, 10);
    qa('input[type="date"]').forEach(input => {
        if (!input.min) input.min = localToday;
    });
    qa('input[type="datetime-local"]').forEach(input => {
        if (!input.min) {
            const future = new Date(Date.now() + 30 * 60 * 1000);
            input.min = new Date(future.getTime() - future.getTimezoneOffset() * 60000)
                .toISOString().slice(0, 16);
        }
    });

    const toastContainer = (() => {
        let container = q('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            container.setAttribute('aria-live', 'polite');
            document.body.appendChild(container);
        }
        return container;
    })();

    window.DineVista = window.DineVista || {};
    window.DineVista.toast = (message, type = 'success') => {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        toastContainer.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            setTimeout(() => toast.remove(), 250);
        }, 3200);
    };

    const menuSearch = q('[data-menu-search]');
    const filterButtons = qa('[data-menu-filter]');
    const menuItems = qa('[data-menu-item]');
    const emptyState = q('[data-menu-empty]');
    let activeCategory = 'all';

    const filterMenu = () => {
        if (!menuItems.length) return;
        const term = (menuSearch?.value || '').trim().toLowerCase();
        let visible = 0;
        menuItems.forEach(item => {
            const category = item.dataset.category || '';
            const text = item.textContent.toLowerCase();
            const matchesCategory = activeCategory === 'all' || category === activeCategory;
            const matchesSearch = !term || text.includes(term);
            const show = matchesCategory && matchesSearch;
            item.style.display = show ? '' : 'none';
            if (show) visible += 1;
        });
        if (emptyState) emptyState.style.display = visible ? 'none' : 'block';
    };

    filterButtons.forEach(button => {
        button.addEventListener('click', () => {
            activeCategory = button.dataset.menuFilter || 'all';
            filterButtons.forEach(item => item.classList.toggle('active', item === button));
            filterMenu();
        });
    });
    menuSearch?.addEventListener('input', filterMenu);

    const reservationSummary = q('[data-reservation-summary]');
    const reservationFields = qa('[data-reservation-field]');
    const updateReservationSummary = () => {
        if (!reservationSummary) return;
        const values = Object.fromEntries(reservationFields.map(field => [field.name, field.value]));
        const dateText = values.date
            ? new Date(`${values.date}T00:00:00`).toLocaleDateString('en-LK', { dateStyle: 'medium' })
            : 'Select a date';
        const areaText = values.seatingArea
            ? values.seatingArea.replaceAll('_', ' ').toLowerCase()
            : 'Not selected';
        reservationSummary.innerHTML = `
            <div class="summary-row"><span>Date</span><strong>${dateText}</strong></div>
            <div class="summary-row"><span>Time</span><strong>${formatTime(values.time) || 'Select a time'}</strong></div>
            <div class="summary-row"><span>Guests</span><strong>${escapeHtml(values.partySize || '0')}</strong></div>
            <div class="summary-row"><span>Seating</span><strong>${escapeHtml(titleCase(areaText))}</strong></div>
        `;
    };
    reservationFields.forEach(field => field.addEventListener('input', updateReservationSummary));
    updateReservationSummary();

    qa('[data-prefill-reservation]').forEach(link => {
        link.addEventListener('click', () => {
            const pairs = {
                date: link.dataset.date,
                time: link.dataset.time,
                partySize: link.dataset.party,
                seatingArea: link.dataset.area
            };
            Object.entries(pairs).forEach(([name, value]) => {
                const field = q(`[name="${name}"]`, q('[data-reservation-form]') || document);
                if (field && value) field.value = value;
            });
            updateReservationSummary();
        });
    });

    const checkout = q('[data-order-checkout]');
    const orderTypeInputs = qa('input[name="orderType"]', checkout || document);
    const reservationOrderField = q('[data-reservation-order-field]');
    const pickupOrderField = q('[data-pickup-order-field]');
    const updateOrderFields = () => {
        const selected = q('input[name="orderType"]:checked', checkout || document)?.value || 'TAKEAWAY';
        if (reservationOrderField) reservationOrderField.hidden = selected === 'TAKEAWAY';
        if (pickupOrderField) pickupOrderField.hidden = selected !== 'TAKEAWAY';
        const reservationSelect = q('[name="reservationReference"]', checkout || document);
        const requestedFor = q('[name="requestedFor"]', checkout || document);
        if (reservationSelect) reservationSelect.required = selected !== 'TAKEAWAY';
        if (requestedFor) requestedFor.required = selected === 'TAKEAWAY';
    };
    orderTypeInputs.forEach(input => input.addEventListener('change', updateOrderFields));
    updateOrderFields();

    qa('[data-dialog-open]').forEach(button => {
        button.addEventListener('click', () => {
            const dialog = document.getElementById(button.dataset.dialogOpen);
            dialog?.classList.add('open');
            document.body.style.overflow = 'hidden';
            q('textarea, input, select', dialog || document)?.focus();
        });
    });
    qa('[data-dialog-close]').forEach(button => {
        button.addEventListener('click', () => closeDialog(button.closest('[data-dialog]')));
    });
    qa('[data-dialog]').forEach(dialog => {
        dialog.addEventListener('click', event => {
            if (event.target === dialog) closeDialog(dialog);
        });
    });
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') qa('[data-dialog].open').forEach(closeDialog);
    });

    const eventSummary = q('[data-event-summary]');
    const eventFields = qa('[data-event-field]');
    const updateEventSummary = () => {
        if (!eventSummary) return;
        const values = Object.fromEntries(eventFields.map(field => [field.name, field.value]));
        const dateText = values.eventDate
            ? new Date(`${values.eventDate}T00:00:00`).toLocaleDateString('en-LK', { dateStyle: 'medium' })
            : 'Select a date';
        eventSummary.innerHTML = `
            <div class="summary-row"><span>Event</span><strong>${escapeHtml(values.eventType || 'Not selected')}</strong></div>
            <div class="summary-row"><span>Package</span><strong>${escapeHtml(values.packageName || 'Not selected')}</strong></div>
            <div class="summary-row"><span>Date</span><strong>${dateText}</strong></div>
            <div class="summary-row"><span>Guests</span><strong>${escapeHtml(values.guestCount || '0')}</strong></div>
        `;
    };
    eventFields.forEach(field => field.addEventListener('input', updateEventSummary));
    updateEventSummary();

    qa('[data-package-select]').forEach(link => {
        link.addEventListener('click', () => {
            if (link.dataset.packageSelect) {
                sessionStorage.setItem('dinevista-selected-package', link.dataset.packageSelect);
            }
        });
    });
    const packageSelect = q('select[name="packageName"]');
    if (packageSelect) {
        const selectedPackage = sessionStorage.getItem('dinevista-selected-package');
        if (selectedPackage && qa('option', packageSelect).some(option => option.value === selectedPackage)) {
            packageSelect.value = selectedPackage;
            packageSelect.dispatchEvent(new Event('input'));
            sessionStorage.removeItem('dinevista-selected-package');
        }
    }

    const counterEls = qa('[data-counter]');
    if ('IntersectionObserver' in window && counterEls.length) {
        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (!entry.isIntersecting) return;
                const el = entry.target;
                const target = Number(el.dataset.counter || 0);
                const suffix = el.dataset.suffix || '';
                const duration = 900;
                const start = performance.now();
                const animate = now => {
                    const progress = Math.min((now - start) / duration, 1);
                    const eased = 1 - Math.pow(1 - progress, 3);
                    el.textContent = `${Math.round(target * eased)}${suffix}`;
                    if (progress < 1) requestAnimationFrame(animate);
                };
                requestAnimationFrame(animate);
                observer.unobserve(el);
            });
        }, { threshold: 0.35 });
        counterEls.forEach(el => observer.observe(el));
    }

    const revenueCanvas = q('[data-revenue-chart]');
    if (revenueCanvas) {
        const drawChart = () => {
            const rect = revenueCanvas.getBoundingClientRect();
            const ratio = window.devicePixelRatio || 1;
            revenueCanvas.width = Math.max(300, rect.width * ratio);
            revenueCanvas.height = 260 * ratio;
            const ctx = revenueCanvas.getContext('2d');
            ctx.scale(ratio, ratio);
            const width = rect.width;
            const height = 260;
            const data = [42, 58, 51, 74, 69, 88, 96];
            const pad = 30;
            ctx.clearRect(0, 0, width, height);
            const themeStyles = getComputedStyle(document.documentElement);
            ctx.strokeStyle = themeStyles.getPropertyValue('--line').trim() || '#eadfd5';
            ctx.lineWidth = 1;
            for (let i = 0; i < 5; i += 1) {
                const y = pad + i * ((height - pad * 2) / 4);
                ctx.beginPath();
                ctx.moveTo(pad, y);
                ctx.lineTo(width - pad, y);
                ctx.stroke();
            }
            const points = data.map((value, index) => ({
                x: pad + index * ((width - pad * 2) / (data.length - 1)),
                y: height - pad - (value / 110) * (height - pad * 2)
            }));
            const gradient = ctx.createLinearGradient(0, pad, 0, height - pad);
            gradient.addColorStop(0, themeStyles.getPropertyValue('--chart-fill').trim() || 'rgba(233,111,61,.28)');
            gradient.addColorStop(1, themeStyles.getPropertyValue('--chart-fill-transparent').trim() || 'rgba(233,111,61,0)');
            ctx.beginPath();
            ctx.moveTo(points[0].x, height - pad);
            points.forEach(point => ctx.lineTo(point.x, point.y));
            ctx.lineTo(points[points.length - 1].x, height - pad);
            ctx.closePath();
            ctx.fillStyle = gradient;
            ctx.fill();
            ctx.beginPath();
            points.forEach((point, index) => index ? ctx.lineTo(point.x, point.y) : ctx.moveTo(point.x, point.y));
            ctx.strokeStyle = themeStyles.getPropertyValue('--brand').trim() || '#e96f3d';
            ctx.lineWidth = 3;
            ctx.stroke();
        };
        drawChart();
        window.addEventListener('resize', drawChart);
        window.addEventListener('dinevista:themechange', drawChart);
    }

    function closeDialog(dialog) {
        if (!dialog) return;
        dialog.classList.remove('open');
        document.body.style.overflow = '';
    }

    function formatTime(value) {
        if (!value || !value.includes(':')) return value;
        const [hours, minutes] = value.split(':').map(Number);
        const date = new Date();
        date.setHours(hours, minutes, 0, 0);
        return date.toLocaleTimeString('en-LK', { hour: 'numeric', minute: '2-digit' });
    }

    function titleCase(value) {
        return String(value).replace(/\b\w/g, char => char.toUpperCase());
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }
})();
