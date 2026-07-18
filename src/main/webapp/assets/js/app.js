(() => {
    'use strict';

    const q = (selector, scope = document) => scope.querySelector(selector);
    const qa = (selector, scope = document) => Array.from(scope.querySelectorAll(selector));

    const menuToggle = q('[data-menu-toggle]');
    const mainNav = q('[data-main-nav]');
    if (menuToggle && mainNav) {
        menuToggle.addEventListener('click', () => {
            const isOpen = mainNav.classList.toggle('open');
            menuToggle.setAttribute('aria-expanded', String(isOpen));
        });
        qa('a', mainNav).forEach(link => link.addEventListener('click', () => {
            mainNav.classList.remove('open');
            menuToggle.setAttribute('aria-expanded', 'false');
        }));
        document.addEventListener('click', event => {
            if (!mainNav.contains(event.target) && !menuToggle.contains(event.target)) {
                mainNav.classList.remove('open');
                menuToggle.setAttribute('aria-expanded', 'false');
            }
        });
    }

    const today = new Date();
    const isoToday = today.toISOString().split('T')[0];
    qa('input[type="date"][data-min-today]').forEach(input => {
        input.min = isoToday;
    });
    qa('input[type="date"][data-min-event]').forEach(input => {
        const eventDate = new Date();
        eventDate.setDate(eventDate.getDate() + 7);
        input.min = eventDate.toISOString().split('T')[0];
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

    const CART_KEY = 'dinevista-cart-v1';
    let cart = [];
    try {
        cart = JSON.parse(localStorage.getItem(CART_KEY) || '[]');
        if (!Array.isArray(cart)) cart = [];
    } catch (error) {
        cart = [];
    }

    const money = value => `LKR ${Number(value).toLocaleString('en-LK', { maximumFractionDigits: 0 })}`;
    const saveCart = () => localStorage.setItem(CART_KEY, JSON.stringify(cart));
    const cartCountEls = qa('[data-cart-count]');
    const cartItemsEl = q('[data-cart-items]');
    const cartSubtotalEl = q('[data-cart-subtotal]');
    const cartServiceEl = q('[data-cart-service]');
    const cartTotalEl = q('[data-cart-total]');
    const checkoutBtn = q('[data-checkout]');

    const renderCart = () => {
        const totalQty = cart.reduce((sum, item) => sum + item.qty, 0);
        cartCountEls.forEach(el => el.textContent = String(totalQty));
        if (!cartItemsEl) return;

        if (!cart.length) {
            cartItemsEl.innerHTML = '<div class="cart-empty">Your cart is waiting for something delicious.</div>';
        } else {
            cartItemsEl.innerHTML = cart.map(item => `
                <div class="cart-item" data-cart-row="${item.id}">
                    <div>
                        <strong>${escapeHtml(item.name)}</strong>
                        <span>${money(item.price)} each</span>
                    </div>
                    <div class="qty-controls">
                        <button class="qty-btn" type="button" data-qty-action="minus" data-id="${item.id}" aria-label="Reduce quantity">-</button>
                        <strong>${item.qty}</strong>
                        <button class="qty-btn" type="button" data-qty-action="plus" data-id="${item.id}" aria-label="Increase quantity">+</button>
                    </div>
                </div>
            `).join('');
        }

        const subtotal = cart.reduce((sum, item) => sum + item.price * item.qty, 0);
        const service = subtotal > 0 ? Math.round(subtotal * 0.05) : 0;
        if (cartSubtotalEl) cartSubtotalEl.textContent = money(subtotal);
        if (cartServiceEl) cartServiceEl.textContent = money(service);
        if (cartTotalEl) cartTotalEl.textContent = money(subtotal + service);
        if (checkoutBtn) checkoutBtn.disabled = !cart.length;
    };

    const escapeHtml = text => String(text)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');

    qa('[data-add-cart]').forEach(button => {
        button.addEventListener('click', () => {
            const id = button.dataset.id;
            const name = button.dataset.name;
            const price = Number(button.dataset.price || 0);
            const found = cart.find(item => item.id === id);
            if (found) found.qty += 1;
            else cart.push({ id, name, price, qty: 1 });
            saveCart();
            renderCart();
            window.DineVista.toast(`${name} added to your cart.`);
        });
    });

    cartItemsEl?.addEventListener('click', event => {
        const button = event.target.closest('[data-qty-action]');
        if (!button) return;
        const item = cart.find(entry => entry.id === button.dataset.id);
        if (!item) return;
        item.qty += button.dataset.qtyAction === 'plus' ? 1 : -1;
        cart = cart.filter(entry => entry.qty > 0);
        saveCart();
        renderCart();
    });

    const modalBackdrop = q('[data-modal]');
    const openModal = () => {
        if (!modalBackdrop) return;
        modalBackdrop.classList.add('open');
        document.body.style.overflow = 'hidden';
    };
    const closeModal = () => {
        if (!modalBackdrop) return;
        modalBackdrop.classList.remove('open');
        document.body.style.overflow = '';
    };
    checkoutBtn?.addEventListener('click', openModal);
    qa('[data-modal-close]').forEach(button => button.addEventListener('click', closeModal));
    modalBackdrop?.addEventListener('click', event => {
        if (event.target === modalBackdrop) closeModal();
    });
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') closeModal();
    });

    const checkoutForm = q('[data-checkout-form]');
    checkoutForm?.addEventListener('submit', event => {
        event.preventDefault();
        if (!checkoutForm.reportValidity()) return;
        const reference = `DV-O-${Math.random().toString(36).slice(2, 8).toUpperCase()}`;
        cart = [];
        saveCart();
        renderCart();
        closeModal();
        checkoutForm.reset();
        window.DineVista.toast(`Order ${reference} was created successfully.`);
    });

    renderCart();

    const reservationSummary = q('[data-reservation-summary]');
    const reservationFields = qa('[data-reservation-field]');
    const updateReservationSummary = () => {
        if (!reservationSummary) return;
        const values = Object.fromEntries(reservationFields.map(field => [field.name, field.value]));
        const dateText = values.date ? new Date(`${values.date}T00:00:00`).toLocaleDateString('en-LK', { dateStyle: 'medium' }) : 'Select a date';
        reservationSummary.innerHTML = `
            <div class="summary-row"><span>Date</span><strong>${dateText}</strong></div>
            <div class="summary-row"><span>Time</span><strong>${values.time || 'Select a time'}</strong></div>
            <div class="summary-row"><span>Guests</span><strong>${values.partySize || '0'}</strong></div>
            <div class="summary-row"><span>Seating</span><strong>${values.seatingArea || 'Not selected'}</strong></div>
        `;
    };
    reservationFields.forEach(field => field.addEventListener('input', updateReservationSummary));
    updateReservationSummary();

    const eventSummary = q('[data-event-summary]');
    const eventFields = qa('[data-event-field]');
    const updateEventSummary = () => {
        if (!eventSummary) return;
        const values = Object.fromEntries(eventFields.map(field => [field.name, field.value]));
        const dateText = values.eventDate ? new Date(`${values.eventDate}T00:00:00`).toLocaleDateString('en-LK', { dateStyle: 'medium' }) : 'Select a date';
        eventSummary.innerHTML = `
            <div class="summary-row"><span>Event</span><strong>${values.eventType || 'Not selected'}</strong></div>
            <div class="summary-row"><span>Package</span><strong>${values.packageName || 'Not selected'}</strong></div>
            <div class="summary-row"><span>Date</span><strong>${dateText}</strong></div>
            <div class="summary-row"><span>Guests</span><strong>${values.guestCount || '0'}</strong></div>
        `;
    };
    eventFields.forEach(field => field.addEventListener('input', updateEventSummary));
    updateEventSummary();

    qa('[data-package-select]').forEach(link => {
        link.addEventListener('click', event => {
            const packageValue = link.dataset.packageSelect;
            if (!packageValue) return;
            sessionStorage.setItem('dinevista-selected-package', packageValue);
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
            ctx.strokeStyle = '#eadfd5';
            ctx.lineWidth = 1;
            for (let i = 0; i < 5; i++) {
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
            gradient.addColorStop(0, 'rgba(233,111,61,.28)');
            gradient.addColorStop(1, 'rgba(233,111,61,0)');
            ctx.beginPath();
            ctx.moveTo(points[0].x, height - pad);
            points.forEach(point => ctx.lineTo(point.x, point.y));
            ctx.lineTo(points[points.length - 1].x, height - pad);
            ctx.closePath();
            ctx.fillStyle = gradient;
            ctx.fill();
            ctx.beginPath();
            points.forEach((point, index) => index ? ctx.lineTo(point.x, point.y) : ctx.moveTo(point.x, point.y));
            ctx.strokeStyle = '#e96f3d';
            ctx.lineWidth = 3;
            ctx.lineJoin = 'round';
            ctx.lineCap = 'round';
            ctx.stroke();
            points.forEach(point => {
                ctx.beginPath();
                ctx.arc(point.x, point.y, 4.5, 0, Math.PI * 2);
                ctx.fillStyle = '#fff';
                ctx.fill();
                ctx.strokeStyle = '#e96f3d';
                ctx.lineWidth = 3;
                ctx.stroke();
            });
        };
        drawChart();
        window.addEventListener('resize', drawChart);
    }
})();
