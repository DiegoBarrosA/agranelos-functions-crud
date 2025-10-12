# Agranelos Testing Container
FROM fedora:39

# Install required tools for testing
RUN dnf update -y && \
    dnf install -y \
    curl \
    jq \
    bash \
    coreutils \
    findutils \
    gawk \
    grep \
    sed \
    which && \
    dnf clean all

# Create testing user
RUN useradd -m -u 1000 tester

# Copy test scripts
COPY scripts/testing/ /opt/testing/
RUN chmod +x /opt/testing/*.sh

# Set ownership
RUN chown -R tester:tester /opt/testing

# Create workspace
WORKDIR /workspace
RUN chown tester:tester /workspace

# Switch to non-root user
USER tester

# Set default environment variables
ENV REST_API_URL="http://host.containers.internal:7071/api"
ENV GRAPHQL_API_URL="http://host.containers.internal:7071/api/graphql"

# Default command runs all tests
CMD ["/opt/testing/test-all-apis.sh"]