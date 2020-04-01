"use strict";

const path = require("path");

const fs = jest.genMockFromModule("fs");

let mockFiles = Object.create(null);
fs.__setMockFiles = newMockFiles => {
  mockFiles = newMockFiles.reduce((files, file) => {
    const dir = path.dirname(file).replace(/\\/g, "/");
    if (!files[dir]) {
      files[dir] = [];
    }
    files[dir].push(path.basename(file));
    return files;
  }, Object.create(null));
};

fs.__resetMockFiles = () => {
  mockFiles = Object.create(null);
};

fs.__getMockFiles = () => mockFiles;

let mockDirectories;
fs.__resetMockDirectories = () => {
  mockDirectories = [];
};

fs.__getMockDirectories = () => mockDirectories;

fs.mkdirSync = directory => {
  const dir = directory.replace(/\\/g, "/");
  mockDirectories.push(dir);
};

fs.existsSync = directory => {
  const dir = path.dirname(directory);
  const name = path.basename(directory);
  return Object.keys(mockFiles).includes(dir) && mockFiles[dir].includes(name);
};

fs.readdirSync = directory => mockFiles[directory] || [];

fs.writeFileSync = (file, data) => {
  const dir = path.dirname(file).replace(/\\/g, "/");
  if (!mockFiles[dir]) {
    mockFiles[dir] = {};
  }
  mockFiles[dir][path.basename(file)] = data;
};

module.exports = fs;
