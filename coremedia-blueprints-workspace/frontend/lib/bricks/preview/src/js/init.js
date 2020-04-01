import { init } from "./toggle";
import { addNodeDecoratorBySelector } from "@coremedia/brick-node-decoration-service";

addNodeDecoratorBySelector(".toggle-item", init);
