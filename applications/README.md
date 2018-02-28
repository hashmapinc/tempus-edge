# Edge Applications

## Standards
- Application folders will be all lower case with '-' to separate words; no underscores, no spaces, no camelCase. This is to conform with Docker image naming conventions.
- Application folders shall contain Dockerfiles defining the applicaiton image.
- Applications shall contain a README.md describing the application and usage. This includes instructions for building and running both the Docker image and the source code.
- Build files / directories shall not be committed to the repository. This means no JAR files, no \*.pyc files, and no mvn \*\*/target/ build directories.
- All commits shall result in buildable code. The code may be buggy, but do not commit unbuildable code.


NOTE: Please submit pull requests to udpate these rules if you think something else makes more sense!