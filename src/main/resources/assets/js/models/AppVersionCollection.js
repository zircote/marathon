define([
  "models/SortableCollection",
  "models/AppVersion"
], function(SortableCollection, AppVersion) {
  return SortableCollection.extend({
    model: AppVersion,

    initialize: function(models, options) {
      this.options = options;
      // this.setComparator("updatedAt");
      // this.sort();
      console.log('created', this.options);
    },

    parse: function(response) {
      console.log('re', response);
      return response.versions;
    },

    url: function() {
      console.log('this.options.appId', this.options.appId);
      return "/v2/apps/" + this.options.appId + "/versions";
    }
  });
});
