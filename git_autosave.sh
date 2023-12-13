#!/bin/bash

# Navigate to your project directory
cd /users/rishijivani/MonopolyDeal-android-backup
# Add all changes
git add .

# Commit changes with a timestamp
git commit -m "Auto-save at $(date)"

# Push to GitHub (assuming the branch is 'main'; replace with your branch name if different)
git push origin main

