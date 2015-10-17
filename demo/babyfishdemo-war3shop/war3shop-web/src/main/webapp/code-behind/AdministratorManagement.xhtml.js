/**
 * @author Tao Chen(&#38472;&#28059;)
 */
function AdministratorManagement() {
    
    //ioc: @UiField
    this.administratorSpecification = null;
    
    //ioc: @UiField
    this.administratorGrid = null;
}

//ioc: @UiHandler
AdministratorManagement.prototype.init = function() {
    var that = this;
    this.administratorGrid.data("controller").specification(function() {
        return that.administratorSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
AdministratorManagement.prototype.administratorSpecification_specificationchanged = function(e) {
    this.administratorGrid.data("controller").refresh();
};
