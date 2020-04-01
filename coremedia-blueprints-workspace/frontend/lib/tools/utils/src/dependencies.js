"use strict";

const closestPackage = require("closest-package");
const packages = require("./packages");
const { getInstallationPath } = require("./workspace");

/**
 * Represents a dependency with sub dependencies.
 *
 * @param name the name of the dependency revolved from the pkg
 * @param version the version of the dependency resolved from the pkg
 * @param pkgPath the path to the package.json of the package the dependency points to
 * @param dependencies (optional) the subdependencies of the dependency
 * @constructor
 */
class NodeModule {
  constructor(name, version, pkgPath, dependencies = null) {
    this._name = name;
    this._version = version;
    this._pkgPath = pkgPath;
    this._dependencies = dependencies || [];
  }

  /**
   * the name of the dependency revolved from the pkg
   * @return {string}
   */
  getName() {
    return this._name;
  }

  // noinspection JSUnusedGlobalSymbols
  /**
   * the version of the dependency resolved from the pkg
   * @return {string}
   */
  getVersion() {
    return this._version;
  }

  /**
   * The path to the package.json of the package the dependency points to
   * @return {string}
   */
  getPkgPath() {
    return this._pkgPath;
  }

  /**
   * The direct dependencies of the dependency
   * @return {Array<NodeModule>}
   */
  getDependencies() {
    return this._dependencies;
  }

  /**
   * Adds a given direct dependency
   * @param {NodeModule} dependency
   */
  addDependency(dependency) {
    this.getDependencies().push(dependency);
  }
}

const ACCEPT_ALL = () => true;

/**
 * @callback resolveDependenciesAcceptCallback
 * @param {NodeModule} dependencyName the dependency to check
 * @param {string} requiredFrom the path to the pkg that depends on the given dependency
 * @return {boolean} if false the resolving will be stopped at that branch.
 */

/**
 *
 * @param pkgPath {String}
 * @param accept  {resolveDependenciesAcceptCallback} (optional) checks if a dependency is included in the result
 * @param parentDependency
 * @param resolvedDependenciesByName {Object} a list of already resolved dependencies
 * @return {NodeModule} the resolved dependency tree
 */
function resolveDependencies(
  pkgPath,
  accept = ACCEPT_ALL,
  parentDependency = null,
  resolvedDependenciesByName = {}
) {
  const pkg = packages.getJsonByFilePath(pkgPath);
  if (!pkg) {
    return null;
  }
  const me = new NodeModule(pkg.name, pkg.version, pkgPath);
  resolvedDependenciesByName[pkg.name] = me;
  if (parentDependency !== null && !accept(me, parentDependency)) {
    return null;
  }

  if (pkg.dependencies instanceof Object) {
    Object.keys(pkg.dependencies).forEach(dependencyName => {
      if (dependencyName in resolvedDependenciesByName) {
        const resolvedDependency = resolvedDependenciesByName[dependencyName];
        if (accept(resolvedDependency, me)) {
          me.addDependency(resolvedDependency);
        }
      } else {
        const dependencyPkgPath = closestPackage.sync(
          getInstallationPath(dependencyName, pkgPath)
        );

        const resolvedDependency = resolveDependencies(
          dependencyPkgPath,
          accept,
          me,
          resolvedDependenciesByName
        );
        if (resolvedDependency) {
          me.addDependency(resolvedDependency);
        }
      }
    });
  }
  return me;
}

/**
 * Get the transitive dependency tree for a given package.
 * The tree is sorted by the order the dependencies are arranged in the respective packages.
 *
 * @param pkgPath the path to the package.json
 * @param accept  {resolveDependenciesAcceptCallback} (optional) checks if a dependency is included in the result
 * @return {Array<NodeModule>}
 */
function getDependencies(pkgPath, accept = ACCEPT_ALL) {
  const dependency = resolveDependencies(pkgPath, accept);
  return dependency ? dependency.getDependencies() : [];
}

function flattenDependencies(
  dependencies,
  collected = [],
  addedDependenciesByName = {}
) {
  if (dependencies instanceof Array) {
    for (let dependency of dependencies) {
      // prevent cyclic dependencies
      const dependencyName = dependency.getName();
      if (!(dependencyName in addedDependenciesByName)) {
        addedDependenciesByName[dependencyName] = true;
        flattenDependencies(
          dependency.getDependencies(),
          collected,
          addedDependenciesByName
        );
        collected.push(dependency);
      }
    }
    return collected;
  }
}

/**
 * Get the transitive dependencies for a given package as flat list.
 * The direct dependencies are sorted by the order the dependencies are arranged in the respective packages.
 * All sub dependencies of a dependency are added before the respective dependency, e.g. if "package-a" depends on
 * "package-b" and "package-b" depends on "package-c" this results in [..."package-c", "package-b", "package-a"...]
 *
 * @param pkgPath the path to the package.json
 * @param accept  {resolveDependenciesAcceptCallback} (optional) checks if a dependency is included in the result
 * @return {Array<NodeModule>}
 */
function getFlattenedDependencies(pkgPath, accept = ACCEPT_ALL) {
  return flattenDependencies(getDependencies(pkgPath, accept));
}

/**
 * Calculates the longestPath to every vertex of a directed tree.
 *
 * @param {Object} edges mapping vertex => vertex
 * @param {String} currentVertex the current vertex, should be the root node for the first call
 * @param {Number} currentWeight the current weight
 * @param {Object} longestPathByVertex the temporary result containing mapping vertex => longestPath
 * @return {Object} mapping vertex => longest path
 */
function calculateLongestPath(
  edges,
  currentVertex,
  currentWeight = 1,
  longestPathByVertex = {}
) {
  if (
    !(currentVertex in longestPathByVertex) ||
    longestPathByVertex[currentVertex] < currentWeight
  ) {
    longestPathByVertex[currentVertex] = currentWeight;
    (edges[currentVertex] || []).forEach(vertex => {
      longestPathByVertex = calculateLongestPath(
        edges,
        vertex,
        currentWeight + 1,
        longestPathByVertex
      );
    });
  }
  return longestPathByVertex;
}

/**
 * Converts and returns a mapping vertex => weight to weight => Array<Vertex>
 */
function getVerticesByWeight(weightByVertex) {
  return Object.keys(weightByVertex).reduce((aggregator, vertex) => {
    const weight = weightByVertex[vertex];
    aggregator[weight] = (aggregator[weight] || []).concat(vertex);
    return aggregator;
  }, {});
}

/**
 * Creates a new directed tree based on the given mapping vertex => weight where all nodes are connected in a way that
 * a deep search will list the nodes by their weight.
 *
 * @param {Object} weightByVertex mapping
 * @returns {Object} edges in form of a mapping of String => Array<String>
 */
function createLowestWeightFirstTree(weightByVertex) {
  const vertexByWeight = getVerticesByWeight(weightByVertex);

  // the last vertex of each group receives edges for the vertices of the next group
  return Object.keys(vertexByWeight).reduce((aggregator, weight) => {
    const verticesWithSameWeight = vertexByWeight[weight];
    const nextVerticesWithSameWeight = vertexByWeight[parseInt(weight) + 1];
    if (nextVerticesWithSameWeight) {
      const lastVertex =
        verticesWithSameWeight[verticesWithSameWeight.length - 1];
      if (lastVertex) {
        aggregator[lastVertex] = nextVerticesWithSameWeight;
      }
    }
    return aggregator;
  }, {});
}

/**
 * Converts and returns a mapping of nodeModuleName => {NodeModule} from an Array<NodeModule>
 *
 * @param {Array<NodeModule>} nodeModules
 * @returns {Object} nodeModuleName => {NodeModule}
 */
function getNodeModulesByName(nodeModules) {
  return flattenDependencies(nodeModules).reduce(
    (aggregator, nextDependency) => {
      aggregator[nextDependency.getName()] = nextDependency;
      return aggregator;
    },
    {}
  );
}

/**
 * Creates a dependency tree.
 *
 * @param {String} basePackageName
 * @param {Object} nodeModuleByModuleName a map of String => Array<NodeModule> representing the direct
 * @param dependencies
 * @returns {Object} edges in form of a mapping of String => Array<String>
 */
function createDependencyTree(
  basePackageName,
  nodeModuleByModuleName,
  dependencies
) {
  return Object.keys(nodeModuleByModuleName).reduce(
    (aggregator, nextModuleName) => {
      aggregator[nextModuleName] = nodeModuleByModuleName[nextModuleName]
        .getDependencies()
        .map(dependency => dependency.getName());
      return aggregator;
    },
    {
      [basePackageName]: dependencies.map(dependency => dependency.getName()),
    }
  );
}

/**
 * Returns a mapping moduleName => Array<NodeModule> which defines a module load order where the every dependent module
 * is loaded before its dependency starting from the given module defined by its basePackageName.
 *
 * Use the mapping to determine which modules needs to be loaded after a module.
 *
 * @param {Array<NodeModule>} dependencies the dependencies of the module
 * @param {String} basePackageName the name of the module to start from
 * @returns {Object} see description
 */
function getDependentsFirstLoadOrder(dependencies, basePackageName) {
  const nodeModuleByModuleName = getNodeModulesByName(dependencies);

  try {
    // 1) use the longest path algorithmn, the module name represents the vertex
    // 2) build a new tree tree where the lowest weight modules are loaded first
    const loadOrder = createLowestWeightFirstTree(
      calculateLongestPath(
        createDependencyTree(
          basePackageName,
          nodeModuleByModuleName,
          dependencies
        ),
        basePackageName
      )
    );
    // transform the tree into a mapping of moduleName => Array<NodeModule>
    return Object.keys(loadOrder).reduce((aggregator, moduleName) => {
      aggregator[moduleName] = loadOrder[moduleName].map(
        moduleName => nodeModuleByModuleName[moduleName]
      );
      return aggregator;
    }, {});
  } catch (e) {
    if (e instanceof RangeError) {
      throw new Error(
        `Could not calculate the correct module load order, please check if there is a cycle in your dependency list:\n\n` +
          `Transitive dependency list:\n${Object.keys(
            nodeModuleByModuleName
          ).join("\n")}`
      );
    }
  }
}

/**
 * Prints a dependency graph in DOT format
 *
 * @param {Array<NodeModule>} dependencies
 * @param {string} packageName package name of the parent
 */
function printDependencyGraphAsDOT(dependencies, packageName) {
  const edges = {};
  function collectEdges(dependencies, parentName) {
    dependencies.forEach(dependency => {
      edges[parentName] = edges[parentName] || new Set();
      edges[parentName].add(dependency.getName());
      collectEdges(dependency.getDependencies(), dependency.getName());
    });
  }
  collectEdges(dependencies, packageName);

  const nodes = Object.keys(edges)
    .map(from => Array.from(edges[from]).map(to => `  "${from}" -> "${to}"`))
    .reduce((acc, next) => acc.concat(next), []);

  console.log("digraph {");
  console.log(nodes.join("\n"));
  console.log("}");
}

// noinspection JSUnusedGlobalSymbols
module.exports = {
  NodeModule,
  flattenDependencies,
  getDependencies,
  getFlattenedDependencies,
  getDependentsFirstLoadOrder,
  printDependencyGraphAsDOT,
};
