package com.coremedia.livecontext.asset.util;

enum InheritFlag {

  INHERIT(true),
  DO_NOT_INHERIT(false);

  final boolean value;

  InheritFlag(boolean value) {
    this.value = value;
  }
}
