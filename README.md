# Element

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)

Element is a DI-like framework for creating standardized "element" objects from configuration files and dependencies.
It's meant for Minecraft developers, but it doesn't depend on any particular platform.

## Table of Contents

- [Background](#background)
- [Install](#install)
- [Usage](#usage)
- [Maintainers](#maintainers)
- [Contributing](#contributing)
- [License](#license)
- [Hosting](#hosting)

## Background

Making data-driven, extensible frameworks is hard. Element was designed to help get rid of as much boilerplate as
possible.

## Install

<a href="https://cloudsmith.io/~steank-f1g/repos/element-QiJ/packages/detail/maven/element-core/latest/a=noarch;xg=com.github.steanky/"><img src="https://api-prd.cloudsmith.io/v1/badges/version/steank-f1g/element-QiJ/maven/element-core/latest/a=noarch;xg=com.github.steanky/?render=true&show_latest=true" alt="Latest version of 'element-core' @ Cloudsmith" /></a>

Element binaries are hosted over on [Cloudsmith](https://cloudsmith.io/~steank-f1g/repos/element-QiJ). You can use
Element by adding it as a dependency to your build management system of choice.

For Gradle, add the repository URL like this:

```groovy
repositories {
    maven {
        url 'https://dl.cloudsmith.io/public/steanky/element/maven/'
    }
}
```

And in your dependencies section:

```groovy
dependencies {
    implementation 'com.github.steanky:element-core:1.0.0'
}
```

(this assumes version 1.0.0, you'll probably want to grab the latest version above)

You can also build binaries directly from source:

```shell
git clone https://github.com/Steanky/Element.git
cd ./Element
./gradlew build
```

## Usage

WIP.

## Maintainers

[Steanky](https://github.com/Steanky)

## Contributing

PRs accepted.

## License

[GNU General Public License v3.0](LICENSE)

## Hosting

[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=for-the-badge)](https://cloudsmith.com)

Package repository hosting is graciously provided by  [Cloudsmith](https://cloudsmith.com).
Cloudsmith is the only fully hosted, cloud-native, universal package management solution, that enables your organization
to create, store and share packages in any format, to any place, with total confidence.
