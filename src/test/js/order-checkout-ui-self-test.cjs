'use strict';

const fs = require('node:fs');

let checks = 0;
const verify = (condition, message) => {
    if (!condition) throw new Error(`Checkout UI regression failed: ${message}`);
    checks += 1;
};

const jsp = fs.readFileSync('src/main/webapp/WEB-INF/views/orders.jsp', 'utf8');
const js = fs.readFileSync('src/main/webapp/assets/js/app.js', 'utf8');
const css = fs.readFileSync('src/main/webapp/assets/css/app.css', 'utf8');

const drawerStart = jsp.indexOf('data-order-cart-drawer');
const drawerEnd = jsp.indexOf('<section class="section-sm" id="my-orders">');
const feedback = jsp.indexOf('data-order-feedback');

verify(drawerStart >= 0 && drawerEnd > drawerStart, 'cart drawer markup is complete');
verify(feedback > drawerStart && feedback < drawerEnd,
        'checkout errors render inside the open cart drawer');
verify(jsp.includes('pageErrors != null && !checkoutHasErrors'),
        'checkout errors are not duplicated behind the drawer');
verify(jsp.includes('action="<%= ctx %>/orders/checkout"'),
        'checkout keeps its server POST route');
verify(jsp.includes('data-cart-has-items="<%= cartHasItems %>"'),
        'checkout receives the real server cart state');
verify(jsp.includes('<% if (cartHasItems) { %>'),
        'the order submit control is only rendered for a non-empty cart');
verify(jsp.includes('data-cart-close>Add dishes before ordering</a>'),
        'an empty cart receives a working route back to the menu');
verify(jsp.includes('name="dineInReservationReference"'),
        'dine-in posts its type-specific reservation field');
verify(jsp.includes('name="preOrderReservationReference"'),
        'pre-order posts its type-specific reservation field');
verify(jsp.includes('data-order-submit>') && !jsp.includes('data-order-submit <%= cartLines'),
        'a valid cart receives an enabled submit button');

verify(js.includes("checkout?.addEventListener('submit'"),
        'checkout has an explicit submission handler');
verify(js.includes("checkout.dataset.cartHasItems !== 'true'"),
        'client validation guards the cart state');
verify(js.includes("selected === 'DINE_IN' && !dineInReservation?.value"),
        'dine-in requires a selected seated reservation');
verify(js.includes("selected === 'PRE_ORDER' && !preOrderReservation?.value"),
        'pre-order requires a selected confirmed reservation');
verify(js.includes('checkout.checkValidity()') && js.includes('checkout.reportValidity()'),
        'customer and fulfilment fields are visibly validated');
verify(js.includes("checkout.dataset.submitting = 'true'"),
        'double submission is blocked');
verify(js.includes('orderSubmit.disabled = true'),
        'the button enters a disabled state only after a valid submission');
verify(js.includes("orderSubmitLabel.textContent = 'Placing order…'"),
        'the user receives immediate valid-submission feedback');
verify(js.includes('control.options.length === 2'),
        'a single eligible reservation is selected automatically');
verify(css.includes('.btn:disabled') && css.includes('cursor: not-allowed'),
        'disabled buttons are visibly different from working buttons');

console.log(`DineVista checkout UI self-test passed: ${checks} checks.`);
