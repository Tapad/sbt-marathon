@(appName: String, appVersion: String, appDependencies: Seq[String])

#!/bin/bash

usage() {
  echo "usage: $(basename "$0") [-h|-v|--dependencies]"
}

print_version() {
  echo "@{appName} @{appVersion}"
}

print_dependencies() {
  echo "@{appName} dependencies:"
  @for(dependency <- appDependencies) {
    echo "@{dependency}"
  }
}

for arg in "$@@"
do
  argi=$((argi + 1))
  next=${args[argi]}

  case $arg in
    -h)
      usage
      ;;
    -v)
      print_version
      ;;
    --dependencies)
      print_dependencies
      ;;
    *)
      echo 'Unknown option'
      usage
      exit 1
  esac
done
