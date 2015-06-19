/**
 * 
 */
var menuapi = menuapi | false;
var panelapi = panelapi | false;
var uploaddone = uploaddone | false;
var editor = editor | false;
var __history = [];

var processing = {
	show : function() {
		$('#processing').show();
	},
	hide : function() {
		$('#processing').hide();
	}
};

$(function() {

	$(window).resize(function() {
		resize();
	});

	var e = $('#ss_tab');
	e.bind('mouseenter', function() {
		var e1 = $(this);
		if (e1.hasClass('info_but_close')) {
			e1.addClass('info_but_close_on');
		} else {
			e1.addClass('info_but_open_on')
		}
	});

	e.bind('mouseleave', function() {
		var e1 = $(this);
		e1.removeClass('info_but_close_on');
		e1.removeClass('info_but_open_on');
	});
	e.click(function() {
		var bar = $(this);
		var menu = $('#menu');
		var panel = $('#panel');
		var toolbar = $('#content .toolbar');

		if (bar.hasClass('info_but_close')) {
			bar.addClass('info_but_open');
			bar.removeClass('info_but_close');
			bar.removeClass('info_but_close_on');

			panel.addClass('full');
			toolbar.addClass('full');
			menu.hide();

			resize();
		} else {
			bar.addClass('info_but_close')
			bar.removeClass('info_but_open');
			bar.removeClass('info_but_open_on');

			panel.removeClass('full');
			toolbar.removeClass('full');

			menu.show();

			resize();
		}
	});
});

function back() {
	if (__history.length > 0) {
		/**
		 * the first one the current
		 */
		// console.log(__history);
		var h = __history.pop();
		if (__history.length > 0) {
			var h = __history.pop();
			show(h);
		}

		/**
		 * push the current in __history also, to make the go ahead works
		 */
		__history.push(h);

		// TODO, erase 1..n-1 if n > 10
	}
}

function show(html) {

	editor = false;
	$('#panel .content').html(html);

	hook();

	$('#panel form').submit(function(e) {
		e.preventDefault();

		var form = e.target;
		var url = form.action;

		var data = $(form).serialize();
		if (editor && editor.srcElement && editor.srcElement.length > 0) {
			var src = editor.srcElement[0];
			data += "&" + src.name + "=" + editor.html();
		}

		processing && processing.show();

		if (form.method == 'get') {
			if(url.indexOf('?') > 0) {
				url += '&' + new Date().getTime();
			} else {
				url += '?' + new Date().getTime();
			}
			$.get(url, data, function(d) {
				show(d);
				processing && processing.hide();
			})
		} else {
			$.post(url, data, function(d) {
				show(d);
				processing && processing.hide();
			})
		}

	})

	editor = false;
	editor = KindEditor.create('textarea[richedit=true]', {
		basePath : '/ke/',
		resizeType : 1,
		allowPreviewEmoticons : false,
		allowImageUpload : true,
		items : [ 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor',
				'bold', 'italic', 'underline', 'removeformat', '|',
				'justifyleft', 'justifycenter', 'justifyright',
				'insertorderedlist', 'insertunorderedlist', '|', 'emoticons',
				'image', 'link' ]
	});

	resize();

}

function hook() {
	$('#panel .content a').each(function(i, e) {
		e = $(e);
		var href = e.attr('href');
		var target = e.attr('target');
		if ((!target) && (href.indexOf('javascript') == -1)) {
			e.click(function() {
				var href = $(this).attr('href');
				load(href);
				return false;
			});
		}
	});
}

function load(uri) {
	processing && processing.show();

	$
			.get(
					uri,
					{},
					function(d) {
						try {
							processing && processing.hide();

							if (__history !== undefined
									&& (__history.length == 0 || __history[__history.length - 1] !== d)) {
								__history.push(d);

								// TODO, erase the 1...n - 9, if n>10
							}
						} catch (err) {

						}

						show(d);
					})
}

function resize() {
	// return;

	var outter = $('#content .outter');
	var inner = $('#content .inner');
	var menu = $('#menu');
	var panel = $('#panel');

	var h = $(window).height() - outter.position().top
			- $('#content').position().top;// - 32;
	outter.css('height', h + 'px');

	if (!menuapi) {
		menuapi = menu.jScrollPane().data('jsp');
	} else {
		menuapi.reinitialise();
	}

	if (!panelapi) {
		panelapi = panel.jScrollPane().data('jsp');
	} else {
		panelapi.reinitialise();
	}
}

function _post(o) {
	var s = '';
	var name = '';
	$('#user-table input:checked').each(function(i, e) {
		if (s.length > 0)
			s += ',';
		s += e.value;
		name = e.name;
	});
	var p = {};
	p[name] = s;
	$.get(o, p, function(d) {
		show(d);
	});
}

function fadeInCart(obj, cb) {
	var o = obj.clone();

	o.css('position', 'absolute');
	o.css('top', obj.offset().top + 'px');
	o.css('left', obj.offset().left + 'px');
	o.css('display', 'inline-block');
	o.css('width', obj.width() + 'px');
	o.css('height', obj.height() + 'px');
	$('body').append(o);
	var c = $('#cart')
	o.animate({
		top : (c.offset().top - 30) + 'px',
		left : (c.offset().left + 100) + 'px',
		width : '0px',
		height : '0px',
		opacity : 0
	}, 1000, function() {
		o.remove();
		cb && cb();
	});
}
