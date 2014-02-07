/** @jsx React.DOM */

define([
  "React",
  "Underscore",
  "jsx!components/NavTabsComponent"
], function(React, _, NavTabsComponent) {
  return React.createClass({
    getInitialState: function() {
      return {
        activeTabId: _.isArray(this.props.children) ?
          this.props.children[0].props.id :
          this.props.children.props.id
      };
    },
    onTabClick: function(id) {
      this.setState({
        activeTabId: id
      });
    },
    render: function() {
      var childNodes = React.Children.map(this.props.children, function(child) {
        return React.addons.cloneWithProps(child, {
          isActive: (child.props.id === this.state.activeTabId)
        });
      }, this);

      var navTabsProps = {
        activeTabId: this.state.activeTabId,
        onTabClick: this.onTabClick,
        tabs: this.props.tabs
      };
      if (this.props.type != null) navTabsProps.type = this.props.type;

      var navTabsComponent = NavTabsComponent(navTabsProps);
      return (
        <div className={this.props.className}>
          {navTabsComponent}
          <div className="tab-content">
            {childNodes}
          </div>
        </div>
      );
    }
  });
});
