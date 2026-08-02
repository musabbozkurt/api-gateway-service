#!/bin/bash

# Maximum number of concurrent jobs
MAX_JOBS=10

# Use a named pipe (FIFO) as a semaphore to manage concurrency
# This is much faster than polling 'jobs' and works on all Bash versions
tmp_fifo="/tmp/build_fifo_$$"
mkfifo "$tmp_fifo"
exec 3<>"$tmp_fifo"
rm "$tmp_fifo"

# Fill the pipe with tokens
for ((i=0; i<$MAX_JOBS; i++)); do
    echo >&3
done

for dir in */; do
    if [ -d "$dir" ] && [ -f "${dir}mvnw" ]; then
        # Take a token from the pipe (blocks if empty)
        read -u 3

        (
            # Run in subshell so 'cd' doesn't affect the main script
            cd "$dir" || exit
            echo "[STARTED] Compiling $dir..."
            if ./mvnw clean install > build.log 2>&1; then
                echo "[SUCCESS] $dir completed."
                rm build.log
            else
                echo "[ERROR] $dir failed. See ${dir}build.log"
            fi
            # Return token to the pipe
            echo >&3
        ) &
    fi
done

# Wait for all background processes to finish
wait
exec 3>&- # Close the file descriptor
echo "All tasks finished."
