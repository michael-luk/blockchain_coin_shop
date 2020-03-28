jQuery(document).ready(function($) {
    var cartWrapper = $('.cd-cart-container');
    //product id - you don't need a counter in your real project but you can use your real product id
    var productId = 0;

    if (cartWrapper.length > 0) {
        //store jQuery objects
        var cartBody = cartWrapper.find('.body')
        var cartList = cartBody.find('ul').eq(0);
        var cartTotal = cartWrapper.find('.checkout').find('span');
        var cartTrigger = cartWrapper.children('.cd-cart-trigger');
        var cartCount = cartTrigger.children('.count')
        //var addToCartBtn = $('.cd-add-to-cart');
        var addToCartBtn = $('.add-button');
        var undo = cartWrapper.find('.undo');
        var undoTimeoutId;

        //add product to cart
        addToCartBtn.on('click', function(event) {
            event.preventDefault();
            addToCart($(this));
        });

        //open/close cart
        cartTrigger.on('click', function(event) {
            event.preventDefault();
            toggleCart();
        });

        //close cart when clicking on the .cd-cart-container::before (bg layer)
        cartWrapper.on('click', function(event) {
            if ($(event.target).is($(this)))
                toggleCart(true);
        });

        //delete an item from the cart
        cartList.on('click', '.delete-item', function(event) {
            event.preventDefault();
            removeProduct($(event.target).parents('.product'));
        });

        //update item quantity
        cartList.on('change', 'select', function(event) {
            quickUpdateCart();
        });

        //reinsert item deleted from the cart
        undo.on('click', 'a', function(event) {
            clearInterval(undoTimeoutId);
            event.preventDefault();
            cartList.find('.deleted').addClass('undo-deleted').one('webkitAnimationEnd oanimationend msAnimationEnd animationend', function() {
                $(this).off('webkitAnimationEnd oanimationend msAnimationEnd animationend').removeClass('deleted undo-deleted').removeAttr('style');
                quickUpdateCart();
            });
            undo.removeClass('visible');
        });
    }

    function toggleCart(bool) {
        var cartIsOpen = (typeof bool === 'undefined') ? cartWrapper.hasClass('cart-open') : bool;

        if (cartIsOpen) {
            cartWrapper.removeClass('cart-open');
            //reset undo
            clearInterval(undoTimeoutId);
            undo.removeClass('visible');
            cartList.find('.deleted').remove();

            setTimeout(function() {
                cartBody.scrollTop(0);
                //check if cart empty to hide it
                if (Number(cartCount.find('li').eq(0).text()) == 0)
                    cartWrapper.addClass('empty');
            }, 500);
        } else {
            cartWrapper.addClass('cart-open');
        }
    }

    function addToCart(trigger) {
        var cartIsEmpty = cartWrapper.hasClass('empty');
        //update cart product list
		
        var     price = trigger.data('price'),
		        proname = trigger.data('proname'),
                proid = trigger.data('proid'),
				quantitys = trigger.data('quantitys');
				
				
				proimg = $(".j_pro_img").attr("src");
				var spec = $(".j_spec_input").val();
				//var price= parseInt($(".j_price").text());



        var price = $(".j_price").text();
		quantitys = parseInt($(".j_add").val());
		

        addProduct(proname, proid, price, proimg,quantitys,spec);
        //console.log();

        //update number of items 
        updateCartCount(cartIsEmpty,quantitys);
        //update total price
        updateCartTotal(price, true,quantitys);
        //show cart
        cartWrapper.removeClass('empty');
    }

    function addProduct(proname, proid, price, proimg,quantitys,spec) {


        //this is just a product placeholder
        //you should insert an item with the selected product info
        //replace productId, productName, price and url with your real product info
        productId = productId + 1;

        var quantity = $("#cd-product-" + proid+"-"+spec).text();
		

        var select = '', productAdded = '';

        //console.log(Number(quantity));
        //console.log(quantity);
        if (quantity == '') {
            var select = '<span class="select">x<i id="cd-product-' + proid + '-'+spec+'">'+quantitys+'</i></span>';
            var productAdded = $('<li class="product"><div class="product-image"><a href="#0"><img src="' + proimg + '" alt="placeholder"></a></div><div class="product-details"><p><a href="#0">' + proname + ' / ' + spec + '</a><span class="price">￥' + price + '</span></p><div class="actions"><a href="#0" class="delete-item">删除</a><div class="quantity"><label for="cd-product-' + proid + '-'+spec+'">件数</label>' + select + '</div></div></div></li>');
            cartList.prepend(productAdded);
        } else {
            quantity = parseInt(quantity);
            //var select = '<span class="select">x<i id="cd-product-'+proid+'">'+quantity+'</i></span>';
            $("#cd-product-" + proid+"-"+spec).html(quantity + quantitys);
        }
		
		$(".badge").text(quantity +quantitys);


        //var productAdded = $('<li class="product"><div class="product-image"><a href="#0"><img src="img/product-preview.png" alt="placeholder"></a></div><div class="product-details"><h3><a href="#0">'+proname+'</a></h3><span class="price">￥'+price+'</span><div class="actions"><a href="#0" class="delete-item">删除</a><div class="quantity"><label for="cd-product-'+ proid +'">件数x</label><span class="select"><select id="cd-product-'+ proid +'" name="quantity"><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option><option value="5">5</option><option value="6">6</option><option value="7">7</option><option value="8">8</option><option value="9">9</option></select></span></div></div></div></li>');
        //cartList.prepend(productAdded);
    }

    function removeProduct(product) {
        clearInterval(undoTimeoutId);
        cartList.find('.deleted').remove();

        var topPosition = product.offset().top - cartBody.children('ul').offset().top,
                productQuantity = Number(product.find('.quantity').find('.select').find('i').text()),
                productTotPrice = Number(product.find('.price').text().replace('￥', '')) * productQuantity;

        product.css('top', topPosition + 'px').addClass('deleted');

        //update items count + total price
        updateCartTotal(productTotPrice, false);
        updateCartCount(true, -productQuantity);
        undo.addClass('visible');

        //wait 8sec before completely remove the item
        undoTimeoutId = setTimeout(function() {
            undo.removeClass('visible');
            cartList.find('.deleted').remove();
        }, 8000);
    }

    function quickUpdateCart() {
        var quantity = 0;
        var price = 0;

        cartList.children('li:not(.deleted)').each(function() {
            var singleQuantity = Number($(this).find('.select').find('i').text());
            quantity = quantity + singleQuantity;
            price = price + singleQuantity * Number($(this).find('.price').text().replace('￥', ''));
        });

        cartTotal.text(price.toFixed(2));
        cartCount.find('li').eq(0).text(quantity);
        cartCount.find('li').eq(1).text(quantity + 1);
    }

    function updateCartCount(emptyCart, quantity) {
        if (typeof quantity === 'undefined') {
            var actual = Number(cartCount.find('li').eq(0).text()) + 1;
            var next = actual + 1;

            if (emptyCart) {
                cartCount.find('li').eq(0).text(actual);
                cartCount.find('li').eq(1).text(next);
            } else {
                cartCount.addClass('update-count');

                setTimeout(function() {
                    cartCount.find('li').eq(0).text(actual);
                }, 150);

                setTimeout(function() {
                    cartCount.removeClass('update-count');
                }, 200);

                setTimeout(function() {
                    cartCount.find('li').eq(1).text(next);
                }, 230);
            }
        } else {
            var actual = Number(cartCount.find('li').eq(0).text()) + quantity;
            var next = actual + 1;

            cartCount.find('li').eq(0).text(actual);
            cartCount.find('li').eq(1).text(next);
        }
    }

    function updateCartTotal(price, bool,quantitys) {
        bool ? cartTotal.text((Number(cartTotal.text()) + Number(price)*quantitys).toFixed(2)) : cartTotal.text((Number(cartTotal.text()) - Number(price)).toFixed(2));
    }
});