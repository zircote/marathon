/** @jsx React.DOM */

define([
  "React",
  "models/App",
  "models/AppVersion",
  "mixins/BackboneMixin",
  "jsx!components/AppVersionComponent"
], function(React, App, AppVersion, BackboneMixin, AppVersionComponent) {
  "use strict";

  var STATES = {
    STATE_LOADING: 0,
    STATE_ERROR: 1,
    STATE_SUCCESS: 2
  };

  return React.createClass({
    displayName: "AppVersionListItemComponent",

    mixins: [BackboneMixin],

    propTypes: {
      app: React.PropTypes.instanceOf(App).isRequired,
      appVersion: React.PropTypes.instanceOf(AppVersion).isRequired,
      onRollback: React.PropTypes.func
    },

    componentDidMount: function() {
      if (this.state.open) {
        this.getVersionDetails();
      }
    },

    getResource: function() {
      return this.props.appVersion;
    },

    getInitialState: function() {
      return {
        open: false,
        fetchState: STATES.STATE_LOADING
      };
    },

    getVersionDetails: function() {
      this.props.appVersion.fetch({
        error: function() {
          this.setState({fetchState: STATES.STATE_ERROR});
        }.bind(this),
        success: function() {
          this.setState({fetchState: STATES.STATE_SUCCESS});
        }.bind(this)
      });
    },

    handleDetailsClick: function(event) {
      if (event.target.type === "radio") {
        // handled by other functions
        return;
      }
      event.preventDefault();
      if (this.state.fetchState !== STATES.STATE_SUCCESS) {
        this.getVersionDetails();
      }
      this.setState({open: !this.state.open});
    },

    render: function() {
      var caretClassSet = React.addons.classSet({
        "clickable text-right col-xs-2": true,
        "dropup": this.state.open
      });
      var versionDate = new Date(this.props.appVersion.get("version"));
      var versionNode;
      if (this.state.fetchState === STATES.STATE_LOADING) {
        versionNode =
          <div className="panel-body">
            <p className="text-center text-muted">
              Loading version details...
            </p>
          </div>;
      } else if (this.state.fetchState === STATES.STATE_ERROR) {
        versionNode =
          <div className="panel-body">
            <p className="text-center text-danger">
              Error fetching version details. Refresh the list to try again.
            </p>
          </div>;
      } else {
        versionNode =
        <div className="panel-body">
          <AppVersionComponent
            className="dl-unstyled"
            app={this.props.app}
            appVersion={this.props.appVersion}
            onRollback={this.props.onRollback} />
        </div>;
      }
      return (
        <div className="panel panel-inverse">
          <div className="panel-heading clickable" onClick={this.handleDetailsClick}>
            <div className="row">
              <div className="col-xs-10">
                <time dateTime={versionDate.toISOString()} title={versionDate.toISOString()}>
                  {versionDate.toLocaleString()}</time>
              </div>
              <div className={caretClassSet}>
                <span className="caret"></span>
              </div>
            </div>
          </div>
            {
              this.state.open ?
                  versionNode :
                  null
            }
        </div>
      );
    }
  });
});
