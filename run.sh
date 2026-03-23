#!/bin/bash

# Go to the main directory
cd "$(dirname "$0")"

echo "📂 Compiling Task Manager for macOS..."

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile with explicit source path and classpath
javac -d bin -sourcepath src src/taskmanager/TaskManagerApp.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful"
    echo "🚀 Launching Task Manager..."
    
    # Run with increased memory and macOS specific flags
    java -Xmx256m -Dapple.awt.UIElement=true -cp bin taskmanager.TaskManagerApp
else
    echo "❌ Compilation failed"
    
    # Show additional debugging info
    echo ""
    echo "📋 Directory Structure:"
    find src -type f | sort
    
    echo ""
    echo "💡 Troubleshooting Tips:"
    echo "1. Make sure all Java files are in the correct package directories"
    echo "2. Check for typos in import statements"
    echo "3. Ensure all referenced classes exist"
fi
