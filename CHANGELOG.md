<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Nx Console Idea Plugin Changelog

## [Unreleased]

## [0.47.1]
### Added
- Add support for node remote interpreter for executing nx tasks using nx npm run scritpts (#90)

## [0.46.3]
### Fixed
- Fix load generators when `id` or `$id` is missing in `schema.json`

## [0.46.2]
### Fixed
- Fix persist don't ask again at project level
- Fix NxAddNxToMonoRepoAction declaration in plugin.xml

## [0.46.1]
### Fixed
- Fix fileNames on for fileType extension

## [0.46.0]
### Added
- Add support for add nx to workspaces(Lerna, Yarn)
- Add support for cra to nx
- Add support for ng add @nrwl/angular

## [0.45.0]
### Added
- Add first support for yarn workspace

### Fixed
- Fix workspace level generator

## [0.44.1]
### Fixed
- Fix minor issues for angular projects

## [0.44.0]
### Added
- Add support for angular projects without nx(Run action from Gutter and Schematics generation)
- Add support for 2022.2 EAP

## [0.43.2]
### Added
- Add new way to load generators/builders (kotlin implemenetation) 
- Add load default for generators from nx.json

### Fixed
- fix issues when root property is missing

## [0.40.0] - 2022-04-12

## [0.39.1]

## [0.39.0]

## [0.38.0]

## [0.36.0]

## [0.35.3]

## [0.35.2]

## [0.35.1]

## [0.35.0]

## [0.34.0]

## [0.33.0]

## [0.32.1]

## [0.32.0]

## [0.31.0]

## [0.30.0]

## [0.29.1]

## [0.29.0]

## [0.28.0]

## [0.27.0]

## [0.26.0]

## [0.25.0]
## [0.25.0] - 2021-04-18
### Added
- Add Grouped tasks in Nx tree view
- Add Install plugins as devDependencies by default

### Fixed
- Fix loading generators in New context menu after adding new one

## [0.24.0]
## [0.24.0]
### Fixed
- Fix loading generators in a React project

## [0.23.0] - 2021-03-13
### Added
- Add Nx Load/Unload App & Libs Action

## [0.22.0] - 2021-02-28
### Added
- Add Nx remove Library or App Action

## [0.21.0] - 2021-02-21
### Added
- Add Nx UI tasks

## [0.20.0] - 2021-02-09
### Added
- Remove AngularJS plugin dependency

## [0.19.0] - 2021-02-04
### Added
- Add Nx plugin project generator
- Support build 211.*

## [0.18.0] - 2021-01-26
### Added
- Add environment field in run configuration

## [0.17.0] - 2021-01-13
### Added
- Add debug support for node process when running Nx Targets

## [0.16.0] - 2021-01-11
### Added
- Support for the new Nx v11 `workspace.json` format

## [0.15.0] - 2021-01-10
### Added
- Ability to switch between generators(schematics)
- Assigned shortcut for Run(cmd+enter) Dry Run(shift+enter) from the Generate UI
- Support autocompletion in project and pth fields

## [0.14.0] - 2021-01-01
### Fixed
- Fix run configuration using @nrwl/cli installed locally

## [0.13.0] - 2020-12-13
### Fixed
- Fix Generate.nx file is not updating

## [0.12.0] - 2020-12-07
### Fixed
- Fix configurable settings exceptions

## [0.11.0] - 2020-11-29
### Added
- Support Generate Schematics on non-angular monorepo preset

## [0.10.0] - 2020-11-14
### Added
- Show generate ui from explorer context menu

## [0.9.0] - 2020-11-09
### Added
- Support for `workspace.json` file for non-angular preset

## [0.8.0] - 2020-11-03
### Added
- Arguments field for run configuration
- fix Crash at the startup #3

## [0.7.0] - 2020-10-26
### Added
- Nx Graph Show Affected Action

## [0.6.0] - 2020-10-07
### Added
- Nx scopes Apps&Libs
- Nx format:write before commit
- Nx settings and plugins management

## [0.5.0] - 2020-10-03
### Added
- Move Nx action
- Nx config file type
- Nx icons for app lib directories
- Nx affected by files and changes

## [0.4.0] - 2020-09-30
### Added
- Preserve args and options when switching from commands to ui
- Navigation from Nx dep-grah ui

## [0.3.0] - 2020-09-28
### Added
- Run Nx generate form ui (add --ui to your command)

## [0.2.0] - 2020-09-25
### Added
- Nx workspace generator
- Run Nx generate from Run Anything

## [0.1.0] - 2020-09-20
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Show Nx Project tasks as toolWindow
- Run Nx Tasks from Run Anything
- Run Nx Tasks from angular.json file
- dep graph file perspective for nx.json