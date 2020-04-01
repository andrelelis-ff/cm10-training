import {
  addNodeDecoratorByData,
  addNodeDecoratorBySelector,
} from "@coremedia/brick-node-decoration-service";
import videoAsMediaElement from "./videoAsMediaElement";
import audioAsMediaElement from "./audioAsMediaElement";

// mediaelement for video
addNodeDecoratorByData({}, "cm-video", videoAsMediaElement);

// mediaelement for audio
addNodeDecoratorBySelector("cm-audio", audioAsMediaElement);
