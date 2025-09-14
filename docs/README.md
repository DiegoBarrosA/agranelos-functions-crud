# Documentation Site

This directory contains the Jekyll-based documentation site for the Agranelos Inventario GraphQL API.

## Structure

- `index.md` - Main documentation page with complete API reference
- `quick-reference.md` - Quick reference guide for common operations
- `_config.yml` - Jekyll configuration
- `assets/css/style.scss` - Custom styling for the documentation
- `Gemfile` - Ruby dependencies for local development

## Local Development

To run the documentation site locally:

1. Install Ruby and Bundler
2. Navigate to the `docs` directory
3. Run `bundle install`
4. Run `bundle exec jekyll serve`
5. Open `http://localhost:4000` in your browser

## GitHub Pages

This site is automatically deployed to GitHub Pages when changes are pushed to the main branch. The workflow is defined in `../.github/workflows/jekyll-gh-pages.yml`.

## API Coverage

The documentation covers:
- GraphQL Queries (productos, producto by ID, bodegas, bodega by ID, health check)
- GraphQL Mutations (create, update, delete for both productos and bodegas)
- Complete schema definitions
- Error handling examples
- Comparison with REST endpoints
- Technical implementation notes