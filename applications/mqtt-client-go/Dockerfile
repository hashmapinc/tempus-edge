# Super slim container for running go applications
FROM alpine:3.7

# Get the name of the compiled executable
ARG EXECUTABLE

# Copy the executable into the image
COPY ${EXECUTABLE} /bin/tempus_edge_application

# Run the executable
CMD ["tempus_edge_application"]