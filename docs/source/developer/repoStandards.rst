.. _developer-repoStandards:

####################
Repository Standards
####################
- Application folders should be all lower case with '-' to separate words; no underscores, no spaces, no camelCase.
- Application folders should contain Dockerfiles defining the applicaiton image.

  - Docker images should be named using the pattern ``hashmapinc/tempus-edge-<APPLICATION-NAME>:<VERSION>``

- Application folders should contain a ``README.md`` describing the application and usage. This includes instructions for building and running both the Docker image and the source code.
- Build files / directories should not be committed to the repository. This means no ``.jar`` files, no ``\*.pyc`` files, and no mvn ``**/target/`` build directories.
- All commits should result in buildable code. The code may be buggy, but please do not commit unbuildable code if you can help it.

NOTE: Please submit pull requests to udpate these rules if you think something else makes more sense!