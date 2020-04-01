// JavaScript in node_modules is not transpiled with babel by default, change this for coremedia packages
module.exports = {
  "transformIgnorePatterns": [
    "/node_modules/(?!@coremedia).+\\.js$"
  ]
};
