#! /bin/bash

echo "Welcome to the ANDROID RELEASE PROCESS!"

read -p "Enter a version name:" version_name 

echo "Creating a new branch for that version..."
git switch -c $version_name

echo "Updating version files for version ${version_name}"
echo "${version_name}" > app/external_version_name.txt
echo "${version_name}" > app/internal_version_name.txt

echo "Ok! Updating Strings for version ${version_name}"

bin/milkrun update-strings
# Exit if that failed:
[ $? -eq 0 ]  || exit 1
echo "Updated strings! Going to add to git add and commit and tag with v${version_name}..."

git add .
git commit -m "Updated translation strings"
git tag "v${version_name}"

read -p "Press enter to build external release"
bundle exec fastlane external
# Exit if that failed:
[ $? -eq 0 ]  || exit 1

read -p "Built new release! Press enter to push to git..."
git push
# Exit if that failed:
[ $? -eq 0 ]  || exit 1
