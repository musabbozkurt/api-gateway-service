# GITHUB_APP_SETUP.md

## Setting Up a GitHub App Token for MB Dependency Version Update Workflow

This document explains how to configure the GitHub App credentials required by
`api-gateway-service/.github/workflows/dependency-update.yml`.

### 1. Create the GitHub App

1. Open **GitHub Settings**.
2. Go to **Developer settings** → **GitHub Apps**.
3. Click **New GitHub App**.
4. Configure the app:
    - **GitHub App name**: choose a unique app name.
    - **Homepage URL**: provide your repository or project URL.
    - **Permissions**:
        - `Metadata`: **Read-only**
        - `Contents`: **Read and write**
        - `Pull requests`: **Read and write**
5. Click **Create GitHub App**.

### 2. Generate and Store Credentials

After creating the app:

- Copy the **App ID**.
- Generate a **Private Key** and download the `.pem` file.

Add them to the target GitHub repository:

- Repository **Variable**: `APP_ID`
- Repository **Secret**: `APP_PRIVATE_KEY`

> The workflow uses `actions/create-github-app-token@v2`, so the private key
> should be stored exactly as the PEM content in the repository secret.

### 3. Install the App on the Repository

1. Open the GitHub App settings page.
2. Select **Install App**.
3. Install it on the `musabbozkurt/api-gateway-service` repository.

### 4. Workflow Behavior

The dependency update workflow will:

- run a read-only dependency check on pull requests,
- run scheduled/manual update jobs on `main`,
- generate a GitHub App token,
- apply Maven dependency updates across all service `pom.xml` files,
- compile only the services whose `pom.xml` files changed,
- create or update an automated pull request.

### 5. Workflow File Reference

- Workflow: `.github/workflows/dependency-update.yml`
- Shared version rules: `version-rules.xml`
