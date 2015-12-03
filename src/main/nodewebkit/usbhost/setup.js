var gui = require('nw.gui');

var menuBar = new gui.Menu({ type: 'menubar' });
var fileMenu = new gui.Menu({ type: 'menubar' });

menuBar.append(new gui.MenuItem({ label: 'File', submenu: fileMenu}));

var mitem1 = new gui.MenuItem({
"label" : "apple",
"click" : function(){ alert("apple clicked"); }
});
var mitem2 = new gui.MenuItem({"label" : "orange"});
fileMenu.append(mitem1);
fileMenu.append(mitem2);

gui.Window.get().menu = menuBar;


