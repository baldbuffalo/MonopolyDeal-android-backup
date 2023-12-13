#!/bin/bash

# Add all changes
git add .

# Commit changes with a timestamp
git commit -m "Auto-save at $(date)"

# Push to GitHub
git push origin main
