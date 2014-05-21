/** @jsx React.DOM */

define([
  "React"
], function(React) {
  return React.createClass({
    propTypes: {
      app: React.PropTypes.object.isRequired,
      onInstall: React.PropTypes.func.isRequired
    },

    render: function() {
      return (
        <li className="panel panel-default">
          <div className="panel-body media">
            <div className="pull-left">
              <img src={"/img/apps/" + this.props.app.id.toLowerCase() + ".png"}
                height="75" width="75" alt="" className="img-thumbnail media-object" />
            </div>
            <div className="media-body text-center">
              <h4 className="media-title">{this.props.app.id}</h4>
              <button type="button" className="btn btn-default" onClick={this.props.onInstall}>
                Install
              </button>
            </div>
          </div>
        </li>
      );
    }
  });
});
