# Url-parameter Add-on for Vaadin 8

This add-on for Vaadin 8 can be used to maintain states of views as url arguments in an easy way. The browser history as well as a kind of deep-link functioality where you can send links around between users of your site is maintained. All you need is setter methods for the argument values once you have configured your views and converters

## Online demo

There is no online demo available. Instead build the project and run the url-parameters-demo, click around, watch the URL, play with the browser history to see how it works

## Download release

Official releases of this add-on are available at Vaadin Directory. For Maven instructions, download and reviews, go to https://vaadin.com/directory/component/url-parameter-add-on

## Building and running demo

git clone git@github.com:wfischlein/urlparameters.git
mvn clean install
cd demo
mvn jetty:run

To see the demo, navigate to http://localhost:8080/

## Development with IDE

For further development of this add-on, the following tool-chain is recommended:
- IDE of your choice
- Browser of your choice

### Importing project

Choose File > New > Project from existing source... > select the root directory and follow the wizzard to make it be an IntelliJ-maven-project

## Release notes

### Version 0.1.0
- initial release

## Roadmap

This component is developed as a hobby with no public roadmap or any guarantees of upcoming releases. 

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. Process for contributing is the following:
- Fork this project
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- Refer to the fixed issue in commit
- Send a pull request for the original project
- Comment on the original issue that you have implemented a fix for it

## License & Author

Add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

Url-parameter is written by Wolfgang Fischlein
