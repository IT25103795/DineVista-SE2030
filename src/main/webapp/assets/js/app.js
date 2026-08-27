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

    qa('input[type="password"]').forEach(input => {
        if (input.dataset.passwordToggleReady === 'true') return;
        input.dataset.passwordToggleReady = 'true';

        const wrapper = document.createElement('span');
        wrapper.className = 'password-field';
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        const toggle = document.createElement('button');
        toggle.className = 'password-toggle';
        toggle.type = 'button';
        toggle.setAttribute('aria-label', 'Show password');
        toggle.setAttribute('aria-pressed', 'false');
        toggle.setAttribute('title', 'Show password');
        toggle.innerHTML =
            '<svg class="password-eye-show" viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
            'stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' +
            '<path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z"/>' +
            '<circle cx="12" cy="12" r="2.7"/></svg>' +
            '<svg class="password-eye-hide" viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
            'stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' +
            '<path d="m3 3 18 18"/>' +
            '<path d="M10.6 6.2A10.7 10.7 0 0 1 12 6c6 0 9.5 6 9.5 6a16.2 16.2 0 0 1-2.1 2.8M6.1 6.1C3.8 7.8 2.5 12 2.5 12s3.5 6 9.5 6a9.8 9.8 0 0 0 3.1-.5"/>' +
            '<path d="M9.9 9.9a3 3 0 0 0 4.2 4.2"/></svg>';
        wrapper.appendChild(toggle);

        toggle.addEventListener('click', () => {
            const show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            const label = show ? 'Hide password' : 'Show password';
            toggle.setAttribute('aria-pressed', String(show));
            toggle.setAttribute('aria-label', label);
            toggle.setAttribute('title', label);
        });
    });

    const notificationMenus = qa('[data-notification-menu]');
    document.addEventListener('click', event => {
        notificationMenus.forEach(menu => {
            if (menu.open && !menu.contains(event.target)) menu.removeAttribute('open');
        });
    });
    document.addEventListener('keydown', event => {
        if (event.key !== 'Escape') return;
        notificationMenus.forEach(menu => {
            if (!menu.open) return;
            menu.removeAttribute('open');
            q('summary', menu)?.focus();
        });
    });
    qa('[data-clear-notifications-form]').forEach(form => form.addEventListener('submit', event => {
        if (!window.confirm('Clear all notifications? This cannot be undone.')) {
            event.preventDefault();
        }
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
            qa('[data-table-spot]').forEach(spot => spot.classList.toggle('selected', spot === link.closest('[data-table-spot]')));
            updateReservationSummary();
        });
    });

    const reservationStepper = q('[data-reservation-stepper]');
    if (reservationStepper) {
        const panels = qa('[data-step-panel]', reservationStepper);
        const stepButtons = qa('[data-step-target]', reservationStepper);
        const review = q('[data-step-review]', reservationStepper);
        let currentStep = 0;

        const validateStep = index => {
            const fields = qa('input, select, textarea', panels[index]).filter(field => !field.disabled);
            const invalid = fields.find(field => !field.checkValidity());
            if (!invalid) return true;
            invalid.reportValidity();
            invalid.focus();
            return false;
        };

        const updateStepReview = () => {
            if (!review) return;
            const value = name => q(`[name="${name}"]`, reservationStepper)?.value || '';
            const dateValue = value('date');
            const dateText = dateValue
                ? new Date(`${dateValue}T00:00:00`).toLocaleDateString('en-LK', { dateStyle: 'full' })
                : 'Not selected';
            const area = value('seatingArea').replaceAll('_', ' ').toLowerCase();
            review.innerHTML = `
                <div><span>Visit</span><strong>${escapeHtml(dateText)} · ${escapeHtml(formatTime(value('time')) || 'No time')}</strong></div>
                <div><span>Party</span><strong>${escapeHtml(value('partySize') || '0')} guest(s) · ${escapeHtml(titleCase(area || 'Any area'))}</strong></div>
                <div><span>Guest</span><strong>${escapeHtml(value('guestName') || 'Not entered')}</strong></div>
                <div><span>Contact</span><strong>${escapeHtml(value('phone') || value('email') || 'Not entered')}</strong></div>
            `;
        };

        const showStep = (index, focusPanel = false) => {
            const nextIndex = Math.max(0, Math.min(index, panels.length - 1));
            currentStep = nextIndex;
            panels.forEach((panel, panelIndex) => {
                const active = panelIndex === nextIndex;
                panel.classList.toggle('active', active);
                panel.setAttribute('aria-hidden', String(!active));
            });
            stepButtons.forEach((button, buttonIndex) => {
                button.classList.toggle('active', buttonIndex === nextIndex);
                button.classList.toggle('complete', buttonIndex < nextIndex);
                if (buttonIndex === nextIndex) button.setAttribute('aria-current', 'step');
                else button.removeAttribute('aria-current');
            });
            if (nextIndex === panels.length - 1) updateStepReview();
            if (focusPanel) q('input, select, textarea, button', panels[nextIndex])?.focus();
        };

        reservationStepper.classList.add('stepper-ready');
        showStep(0);

        qa('[data-step-next]', reservationStepper).forEach(button => button.addEventListener('click', () => {
            if (validateStep(currentStep)) showStep(currentStep + 1, true);
        }));
        qa('[data-step-back]', reservationStepper).forEach(button => button.addEventListener('click', () => {
            showStep(currentStep - 1, true);
        }));
        stepButtons.forEach(button => button.addEventListener('click', () => {
            const target = Number(button.dataset.stepTarget);
            if (target <= currentStep) showStep(target, true);
            else if (validateStep(currentStep)) showStep(currentStep + 1, true);
        }));
        qa('input, select, textarea', reservationStepper).forEach(field => field.addEventListener('input', updateStepReview));
        qa('[data-prefill-reservation]').forEach(link => link.addEventListener('click', () => showStep(0)));
    }

    const checkout = q('[data-order-checkout]');
    const orderTypeInputs = qa('input[name="orderType"]', checkout || document);
    const dineInOrderField = q('[data-dinein-order-field]');
    const preOrderField = q('[data-preorder-order-field]');
    const pickupOrderField = q('[data-pickup-order-field]');
    const orderSubmitLabel = q('[data-order-submit-label]');
    const orderSubmit = q('[data-order-submit]');
    const orderFeedback = q('[data-order-feedback]');
    const orderFeedbackTitle = q('[data-order-feedback-title]');
    const orderFeedbackList = q('[data-order-feedback-list]');
    const restaurantSummaryRows = qa('[data-restaurant-charge], [data-restaurant-total]');
    const takeawaySummaryRow = q('[data-takeaway-total]');
    const clearOrderFeedback = () => {
        if (!orderFeedback) return;
        orderFeedback.hidden = true;
        if (orderFeedbackList) orderFeedbackList.replaceChildren();
    };
    const showOrderFeedback = (messages, title = 'Unable to place the food order.') => {
        if (!orderFeedback || !messages.length) return;
        if (orderFeedbackTitle) orderFeedbackTitle.textContent = title;
        if (orderFeedbackList) {
            orderFeedbackList.replaceChildren(...messages.map(message => {
                const item = document.createElement('li');
                item.textContent = message;
                return item;
            }));
        }
        orderFeedback.hidden = false;
        orderFeedback.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    };
    const setOrderFieldState = (field, active) => {
        if (!field) return;
        field.hidden = !active;
        qa('input, select', field).forEach(control => {
            control.disabled = !active;
            control.required = active;
            if (active && control instanceof HTMLSelectElement
                    && !control.value && control.options.length === 2) {
                control.selectedIndex = 1;
            }
        });
    };
    const updateOrderFields = () => {
        const selected = q('input[name="orderType"]:checked', checkout || document)?.value || 'TAKEAWAY';
        setOrderFieldState(dineInOrderField, selected === 'DINE_IN');
        setOrderFieldState(preOrderField, selected === 'PRE_ORDER');
        setOrderFieldState(pickupOrderField, selected === 'TAKEAWAY');
        restaurantSummaryRows.forEach(row => { row.hidden = selected === 'TAKEAWAY'; });
        if (takeawaySummaryRow) takeawaySummaryRow.hidden = selected !== 'TAKEAWAY';
        if (orderSubmitLabel) {
            orderSubmitLabel.textContent = selected === 'DINE_IN'
                ? 'Place dine-in order'
                : (selected === 'PRE_ORDER' ? 'Place pre-order' : 'Place takeaway order');
        }
    };
    orderTypeInputs.forEach(input => input.addEventListener('change', () => {
        clearOrderFeedback();
        updateOrderFields();
    }));
    qa('input, select, textarea', checkout || document).forEach(control => {
        control.addEventListener('input', clearOrderFeedback);
        control.addEventListener('change', clearOrderFeedback);
    });
    updateOrderFields();

    checkout?.addEventListener('submit', event => {
        if (checkout.dataset.submitting === 'true') {
            event.preventDefault();
            return;
        }

        const selected = q('input[name="orderType"]:checked', checkout)?.value || '';
        const messages = [];
        const dineInReservation = q('#dineInReservationReference', checkout);
        const preOrderReservation = q('#preOrderReservationReference', checkout);
        const takeawayTime = q('#requestedFor', checkout);

        if (checkout.dataset.cartHasItems !== 'true') {
            messages.push('Add at least one available menu item before placing the order.');
        }
        if (selected === 'DINE_IN' && !dineInReservation?.value) {
            messages.push('Select your seated reservation before placing a dine-in order.');
        }
        if (selected === 'PRE_ORDER' && !preOrderReservation?.value) {
            messages.push('Select an eligible confirmed reservation before placing a pre-order.');
        }
        if (selected === 'TAKEAWAY' && !takeawayTime?.value) {
            messages.push('Select a takeaway collection time.');
        }
        if (!checkout.checkValidity()) {
            messages.push('Complete the highlighted customer and fulfilment fields correctly.');
        }

        const uniqueMessages = [...new Set(messages)];
        if (uniqueMessages.length) {
            event.preventDefault();
            showOrderFeedback(uniqueMessages);
            checkout.reportValidity();
            return;
        }

        checkout.dataset.submitting = 'true';
        if (orderSubmit) orderSubmit.disabled = true;
        if (orderSubmitLabel) orderSubmitLabel.textContent = 'Placing order…';
    });

    const cartDrawer = q('[data-order-cart-drawer]');
    if (cartDrawer) {
        const cartBackdrop = q('[data-cart-backdrop]');
        let cartReturnFocus = null;

        cartDrawer.setAttribute('role', 'dialog');
        cartDrawer.setAttribute('aria-modal', 'true');
        cartDrawer.setAttribute('aria-labelledby', 'order-cart-title');
        cartDrawer.setAttribute('aria-hidden', 'true');
        cartDrawer.setAttribute('tabindex', '-1');
        document.body.classList.add('cart-drawer-enabled');

        const openCart = trigger => {
            cartReturnFocus = trigger || document.activeElement;
            document.body.classList.add('cart-drawer-open');
            cartDrawer.setAttribute('aria-hidden', 'false');
            cartBackdrop?.setAttribute('aria-hidden', 'false');
            requestAnimationFrame(() => (q('[data-cart-close]', cartDrawer) || cartDrawer).focus());
        };

        const closeCart = () => {
            document.body.classList.remove('cart-drawer-open');
            cartDrawer.setAttribute('aria-hidden', 'true');
            cartBackdrop?.setAttribute('aria-hidden', 'true');
            if (cartReturnFocus instanceof HTMLElement) cartReturnFocus.focus();
        };

        qa('[data-cart-open]').forEach(trigger => trigger.addEventListener('click', event => {
            event.preventDefault();
            openCart(trigger);
        }));
        qa('[data-cart-close]', cartDrawer).forEach(button => button.addEventListener('click', closeCart));
        cartBackdrop?.addEventListener('click', closeCart);
        document.addEventListener('keydown', event => {
            if (!document.body.classList.contains('cart-drawer-open')) return;
            if (event.key === 'Escape') {
                closeCart();
                return;
            }
            if (event.key !== 'Tab') return;
            const focusable = qa('a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])', cartDrawer)
                .filter(element => element.offsetParent !== null);
            if (!focusable.length) return;
            const first = focusable[0];
            const last = focusable[focusable.length - 1];
            if (event.shiftKey && document.activeElement === first) {
                event.preventDefault();
                last.focus();
            } else if (!event.shiftKey && document.activeElement === last) {
                event.preventDefault();
                first.focus();
            }
        });

        if (window.location.hash === '#order-cart' || cartDrawer.dataset.cartHasErrors === 'true') openCart();
    }

    qa('[data-copy-reference]').forEach(button => {
        button.addEventListener('click', async () => {
            const reference = button.dataset.copyReference || '';
            if (!reference) return;
            try {
                await navigator.clipboard.writeText(reference);
            } catch (error) {
                const helper = document.createElement('textarea');
                helper.value = reference;
                helper.setAttribute('readonly', '');
                helper.style.position = 'fixed';
                helper.style.opacity = '0';
                document.body.appendChild(helper);
                helper.select();
                document.execCommand('copy');
                helper.remove();
            }
            const original = button.textContent;
            button.textContent = 'Copied!';
            window.DineVista.toast(`Reference ${reference} copied.`);
            setTimeout(() => { button.textContent = original; }, 1800);
        });
    });

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
