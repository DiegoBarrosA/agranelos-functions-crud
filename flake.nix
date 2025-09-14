{
  description = "Azure Functions Java project with automatic deployment";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };
    in {
      devShells.${system}.default = pkgs.mkShell {
        buildInputs = with pkgs; [
          openjdk17
          maven
          azure-functions-core-tools
          azure-cli
          nodejs_20
          yarn
          git
          unzip
          curl
          docker
          docker-compose
          postgresql
          # Spring Boot será incluido como dependencia Maven, no necesitamos instalarlo globalmente
        ];
        shellHook = ''
          if [ -f .env ]; then
            set -a
            . ./.env
            set +a
            echo "Loaded environment variables from .env"
          fi
          echo "Azure Functions devShell ready. Use 'mvn clean package' to build and 'func start' to run locally."
          echo "To deploy: run './deploy.sh <your-azure-function-app-name>'"
        '';
      };
      packages.${system}.deploy = pkgs.writeShellScriptBin "deploy" ''
        set -e
        if [ -z "$1" ]; then
          echo "Usage: $0 <azure-function-app-name>"
          exit 1
        fi
        mvn clean package
        func azure functionapp publish "$1"
      '';
    };
}
