/** @jsx React.DOM */

define([
  "React",
  "mixins/BackboneMixin",
  "models/AppVersionCollection",
], function(React, BackboneMixin, AppVersionCollection) {

  return React.createClass({
    displayName: "AppConfigComponent",
    mixins:[BackboneMixin],
    propTypes: {
      model: React.PropTypes.object.isRequired
    },
    componentDidMount: function() {
      this.fetchResource();
    },
    getInitialState: function() {
      return {
        versions: new AppVersionCollection({appId: this.props.model.get("id")})
      };
    },
    getResource: function() {
      return this.state.versions;
    },
    fetchResource: function() {
      this.state.versions.fetch({
        error: function() {
          console.log("error");
        },
        reset: true,
        success: function() {
         console.log("success");
        }
      });
    },
    render: function() {
      var model = this.props.model;
      var cmdNode = (model.get("cmd") == null) ?
        <dd className="text-muted">Unspecified</dd> :
        <dd>{model.get("cmd")}</dd>;
      var constraintsNode = (model.get("constraints").length < 1) ?
        <dd className="text-muted">Unspecified</dd> :
        model.get("constraints").map(function(c) {

          // Only include constraint parts if they are not empty Strings. For
          // example, a hostname uniqueness constraint looks like:
          //
          //     ["hostname", "UNIQUE", ""]
          //
          // it should print "hostname:UNIQUE" instead of "hostname:UNIQUE:", no
          // trailing colon.
          return (
            <dd key={c}>
              {c.filter(function(s) { return s !== ""; }).join(":")}
            </dd>
          );
        });
      var containerNode = (model.get("container") == null) ?
        <dd className="text-muted">Unspecified</dd> :
        <dd>{JSON.stringify(model.get("container"))}</dd>;
      var envNode = (Object.keys(model.get("env")).length === 0) ?
        <dd className="text-muted">Unspecified</dd> :

        // Print environment variables as key value pairs like "key=value"
        Object.keys(model.get("env")).map(function(k) {
          return <dd key={k}>{k + "=" + model.get("env")[k]}</dd>;
        });
      var executorNode = (model.get("executor") === "") ?
        <dd className="text-muted">Unspecified</dd> :
        <dd>{model.get("executor")}</dd>;
      var portsNode = (model.get("ports").length === 0 ) ?
        <dd className="text-muted">Unspecified</dd> :
        <dd>{model.get("ports").join(",")}</dd>;
      var versionNode = (model.get("version") == null) ?
        <dd className="text-muted">Unspecified</dd> :
        <dd>{model.get("version").toLocaleString()}</dd>;
      var urisNode = (model.get("uris").length === 0) ?
        <dd className="text-muted">Unspecified</dd> :
        model.get("uris").map(function(u) {
          return <dd key={u}>{u}</dd>;
        });

      var versionsNode = this.state.versions.map(function(k) {
          return [<dd key={k}>{k + "=" + this.state.versions[k]}</dd>];
        });

      return (
        <dl className="dl-horizontal">
          <dt>Command</dt>
          {cmdNode}
          <dt>Constraints</dt>
          {constraintsNode}
          <dt>Container</dt>
          {containerNode}
          <dt>CPUs</dt>
          <dd>{model.get("cpus")}</dd>
          <dt>Environment</dt>
          {envNode}
          <dt>Executor</dt>
          {executorNode}
          <dt>Instances</dt>
          <dd>{model.get("instances")}</dd>
          <dt>Memory (MB)</dt>
          <dd>{model.get("mem")}</dd>
          <dt>Ports</dt>
          {portsNode}
          <dt>URIs</dt>
          {urisNode}
          <dt>Version</dt>
          {versionNode}
          <dt>Versions</dt>
          {versionsNode}
        </dl>
      );
    }

  });
});
