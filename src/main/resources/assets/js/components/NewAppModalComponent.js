/** @jsx React.DOM */

define([
  "jquery",
  "Underscore",
  "React",
  "ace",
  "mixins/BackboneMixin",
  "jsx!components/FormGroupComponent",
  "jsx!components/ModalComponent"
], function($, _, React, ace, BackboneMixin, FormGroupComponent, ModalComponent) {
  return React.createClass({
    componentDidMount: function() {
      var editor = ace.edit(this.refs.editor.getDOMNode());
      editor.setTheme("ace/theme/tomorrow_night");

      var session = this.session = editor.getSession();
      session.setValue(this.props.model.stringify(2))
      session.setMode("ace/mode/json");
      session.setTabSize(2);
      session.setUseSoftTabs(true);
    },
    destroy: function() {
      this.refs.modalComponent.destroy();
    },
    getResource: function() {
      return this.props.model;
    },
    mixins: [BackboneMixin],
    onSubmit: function(event) {
      event.preventDefault();
      this.props.model.set(JSON.parse(this.session.getValue()));

      if (this.props.model.isValid()) {
        this.props.onCreate();
        this.destroy();
      }
    },
    render: function() {
      var model = this.props.model;

      return (
        <ModalComponent ref="modalComponent" size="lg">
          <form method="post" className="form-horizontal" role="form" onSubmit={this.onSubmit}>
            <div className="modal-header">
              <button type="button" className="close"
                aria-hidden="true" onClick={this.destroy}>&times;</button>
              <h3 className="modal-title">New Application</h3>
            </div>
            <div className="modal-body">
              <div className="row">
                <div className="col-sm-6 editor" ref="editor" />
                <div className="col-sm-6 ace-tomorrow-night ace-tomorrow-night-uneditable ace_editor">
                  <dl>
                    <dt className="ace_variable">cpus</dt>
                    <dd>Number of CPUs to allocate this app</dd>
                    <dt className="ace_variable">id</dt>
                    <dd>Unique ID for the new application</dd>
                    <dt className="ace_variable">instances</dt>
                    <dd>Number of app tasks to run</dd>
                    <dt className="ace_variable">mem</dt>
                    <dd>Amount of memory to allocate this app in megabytes (MB)</dd>
                  </dl>
                  <h5>Optional Attributes</h5>
                  <dl>
                    <dt className="ace_variable">cmd</dt>
                    <dd>String command to start this app</dd>
                    <dt className="ace_variable">constraints</dt>
                    <dd>
                      Array of Arrays of Strings. Valid constraint operators are
                      ["UNIQUE", "CLUSTER", "GROUP_BY"]
                    </dd>
                    <dt className="ace_variable">container</dt>
                    <dd>
                      Hash with a key "image" with a String path to the image
                      location and a key "options" with an Array of String
                      options to pass to the image.
                    </dd>
                    <dt className="ace_variable">env</dt>
                    <dd>
                      Hash of key/value pairs set when running
                      <span className="ace_variable">cmd</span>
                    </dd>
                    <dt className="ace_variable">executor</dt>
                    <dd>
                      Defaults to "/cmd" if
                      <span className="ace_variable">cmd</span> is supplied
                    </dd>
                    <dt className="ace_variable">ports</dt>
                    <dd>
                      Array of ports to assign this app. Zero (0)
                      assigns a random, unused port.
                    </dd>
                    <dt className="ace_variable">uris</dt>
                    <dd>
                      Array of String URIs to download before starting this app
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-link" type="button" onClick={this.destroy}>
                Cancel
              </button>
              <input type="submit" className="btn btn-primary" value="Create" />
            </div>
          </form>
        </ModalComponent>
      );
    }
  });
});
