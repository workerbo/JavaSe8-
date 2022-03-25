Kendo UI 是基于 jQuery 库开发的，Kendo UI widgets 是以 jQuery 插件形式提供的

事件处理函数在事件发生时被调用，该事件处理函数的传入参数包含了事件相关的 JavaScript 对象，你可以通过 e.sender 参数获得触发该事件的 UI 组件【事件、属性、方法、配置】，此外，也可以使用 this 关键字来获取触发事件的 UI 对象引用。

除了使用 jQuery 插件的方法来初始化方法外，每个 Kendo 控制项还可以通过 data 属性来初始化，此时你需要设置 data 的 role 属性，然后调用 kendo.init 方法。

Kendo UI 框架提供了一个易用，高性能的 JavaScript 模板引擎。通过模板可以创建一个 HTML 片段然后可以和 JavaScript 数据合并成最终的 HTML 元素。

用三种方式使用 # 语法：

1. 显示字面量 #=#
2. 显示HTML元素 #：#
3. 执行任意的Javascript代码 #if() {# …#}#




    var template = kendo.template("<div id='box'>#= firstName #</div>");
    var data = { firstName: "Todd" }; //A value in JavaScript/JSON
    var result = template(data); //Pass the data to the compiled template
    $("#example").html(result); //display the result



Kendo UI Validator 支持了客户端校验的便捷方法，它基于 **HTML 5**【type="text"】 的表单校验功能，支持很多**内置**【required  pattern min】的校验规则，同时也提供了**自定义**规则的便捷方法。

Kendo UI Validator 显示合适的错误信息， 每个 HTML 元素也可以通过 validatorMessage 定义一个自定义的错误信息



kendoUI备注功能：

1.Grid的工具按钮  The "search" built-in search panel for the grid.

Commands can be custom or built-in ("cancel", "create", "save", "excel", "pdf").

the string value will be passed as an argument to a [kendo.template()](https://docs.telerik.com/kendo-ui/api/javascript/kendo/methods/template) function.

2.sortable可以点击头排序  search全Grid搜索【只针对当前页面？】 allowCopy方便Excel复制  reorderable 支持列的拖曳交换 resizeable 调整列宽