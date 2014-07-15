/** @jsx React.DOM */

define([
  "React"
], function(React) {
  "use strict";

  return React.createClass({
    getDefaultProps: function() {
      return {
        isActive: false
      };
    },

    render: function() {
      var classSet = React.addons.classSet({
        "active": this.props.isActive,
        "tab-pane": true
      });

      /* jshint trailing:false, quotmark:false, newcap:false */
      return (
        <div className={classSet}>
          {this.props.children}
        </div>
      );
    }
  });
});
