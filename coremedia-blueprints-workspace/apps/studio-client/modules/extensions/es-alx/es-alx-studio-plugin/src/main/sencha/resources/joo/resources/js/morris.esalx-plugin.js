(function() {
  Morris.Line.prototype.displayHoverForPublication = function(index) {
    var _ref;
    if (index != null) {
      (_ref = this.hover).update.apply(_ref, this.hoverContentForPublication(index));
      return this.hilight(index);
    } else {
      this.hover.hide();
      return this.hilight();
    }
  };
  Morris.Line.prototype.hoverContentForPublication = function(index) {
    var content, j, row, y, _i, _len, _ref;
    row = this.data[index];
    if (typeof this.options.hoverCallback === 'function') {
      content = this.options.hoverCallback(index, this.options);
    } else {
      content = "<div class=\" status-published\" style=\"float:none; margin:0 auto;\"></div>";
      content += "<div class='morris-hover-row-label'>" + row.label + "</div>";
      _ref = row.y;
      for (j = _i = 0, _len = _ref.length; _i < _len; j = ++_i) {
        y = _ref[j];
        content += "<div class='morris-hover-point' style='color: " + (this.colorFor(row, j, 'label')) + "'>\n  " + this.options.labels[j] + ":\n  " + (this.yLabelFormat(y)) + "\n</div>";
      }
    }
    return [content, row._x, row._ymax];
  };
})();
