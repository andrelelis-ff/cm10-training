# Managing CoreMedia Extensions

## Getting Started

`cd` into this directory (`workspace-configuration/extensions`)

OR add `-f workspace-configuration/extensions` to every `mvn` command documented here.

For a list of all extension tool "goals" (commands) and their description, call
```
mvn extensions:help
```

For a detailed man page of some goal, including their `-D` options, call
```
mvn extensions:help -Dgoal=... -Ddetail
```

For specific use cases, find the mvn command lines below.

> NOTE: The command `mvn extensions:sync` modifies source files in your workspace. You should only call it when all
your work has been committed and your workspace is in a clean state.

## Listing All Available CoreMedia Extension Points
```
mvn extensions:list-extension-points -q
```

## Listing All Available CoreMedia Extensions
```
mvn extensions:list -q
```

The output indicates inactive extensions by a hash prefix ("#").

## Verifying Extension Consistency

In a clean git repository state, run 
```
mvn extensions:sync
```
There should be no changed files afterwards.

## Disabling Extensions

```
mvn extensions:sync -Ddisable=<extension1>,<extension2>,...
```

Moves all (aggregator) modules belonging to the listed extensions into a profile "inactive-extensions" and
removes their dependencies from the corresponding extension points.

## Removing Extensions

```
mvn extensions:sync -Dremove=<extension1>,<extension2>,...
```

Moves all (aggregator) modules belonging to the listed extensions into a profile "inactive-extensions" and
removes their dependencies from the corresponding extension points.

You could add `-Dprune` to remove even all source code of the listed extensions, but this is discouraged because
deleting files leads to merge conflicts when updating to a new Blueprint git state.

## Enabling Extensions

```
mvn extensions:sync -Denable=<extension1>,<extension2>,...
```

Moves all (aggregator) modules belonging to the listed extensions from the profile "inactive-extensions" to the
normal sub-modules and adds their dependencies to the corresponding extension points.

Note that this only works for disabled extensions, not for extensions that are not even part of the Maven Reactor
when the profile "inactive-extensions" is switched on. To add extensions to the aggregator, see "Adding Projects".

## Managing a Set of Extensions

```
mvn extensions:sync -DextensionsFile=<extensionsFilePathAndName>
```

Emulating the "task input file" of the previous extensions tool, this option allows to specify a file containing
the set of extensions to enable (or disable). All other extensions that are present in the workspace, but not
mentioned in the `extensionsFile`, are removed (or even pruned, see "Removing Extensions").

## Adding Projects

```
mvn extensions:sync -DaddProjects=<project-path1>,<project-path2>,...
```

The Maven projects at the given directory paths are added to their respective aggregator (which already must
exist!) before synchronizing extension state. This is useful to add and enable new extensions that are not yet
part of the Maven Reactor (see "Enabling Extensions").

Even new extension points can be added and used immediately by new extensions.
