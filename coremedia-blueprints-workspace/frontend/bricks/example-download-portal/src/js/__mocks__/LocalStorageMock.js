export default class {
  constructor() {
    this.mockStorage = {};
  }

  setItem(key, val) {
    Object.assign(this.mockStorage, { [key]: val });
  }

  getItem(key) {
    const value = this.mockStorage[key];
    return value !== undefined ? value : null;
  }

  removeItem(key) {
    delete this.mockStorage[key];
  }

  clear() {
    this.mockStorage = {};
  }
}
