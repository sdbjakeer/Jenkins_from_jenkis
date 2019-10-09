/*
 * The MIT License
 *
 * Copyright (c) 2019 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

Behaviour.specify('.hidden-password', 'hidden-password-button', 0, function (e) {
    var secretUpdateBtn = e.querySelector('.hidden-password-update-btn');
    if (secretUpdateBtn === null) return;

    var id = 'hidden-password-' + (iota++);

    secretUpdateBtn.onclick = function () {
        e.querySelector('.hidden-password-field').setAttribute('type', 'password');
        e.querySelector('.hidden-password-placeholder').remove();
        secretUpdateBtn.remove();
        // fix UI bug when DOM changes
        Event.fire(window, 'jenkins:bottom-sticker-adjust');
    };
});

Behaviour.specify('.hidden-for-noscript', 'hidden-for-noscript', 0, function (e) {
    console.info('Element', e, 'set visible');
    e.classList.remove('hidden-for-noscript');
});
