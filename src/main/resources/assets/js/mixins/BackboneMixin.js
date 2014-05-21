define([
], function() {
  // Binds a listener to a component's resource so the component can be updated
  // when the resource changes.
  //
  // An object that uses this mixin must implement `getResource` and return an
  // object that extends `Backbone.Events`. Common use cases are
  // `Backbone.Model` and `Backbone.Collection`.
  return {
    componentDidMount: function() {
      this._boundForceUpdate = this.forceUpdate.bind(this, null);
      this.getResource().on("add remove reset sync", this._boundForceUpdate, this);
    },
    componentWillUnmount: function() {
      this.getResource().off("add remove reset sync", this._boundForceUpdate);
    }
  };
});
