{
  description = "A development environment for Azure Functions with Java 11 and Maven";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.05";

  outputs = { self, nixpkgs }:

    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };
    in {

      devShells.${system}.default = pkgs.mkShell {
        buildInputs = with pkgs; [
          # Java 11
          jdk11

          # Maven for building Java projects
          maven

          # Azure CLI for managing Azure resources
          azure-cli
          azure-functions-core-tools
          # Node.js and npm for Azure Functions Core Tools
          nodejs_24

          # Any other tools you might need
          git
        ];

        # Environment variables or other shell-specific configurations can go here
        shellHook = ''
          echo "Development environment for Azure Functions with Java 11 and Maven is ready!"
        '';
      };

    };
}
