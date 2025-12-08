#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Fix Postman Collection Encoding Issues
This script fixes UTF-8 encoding problems in Postman collection files
"""

import json
import sys

def fix_encoding(file_path):
    print(f"🔧 Fixing Postman Collection Encoding...")
    print(f"📖 Reading file: {file_path}")
    
    try:
        # Read the file with UTF-8 encoding
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        print("🔄 Applying fixes...")
        
        # Define replacement patterns
        replacements = {
            # Emoji fixes (mojibake -> correct emoji)
            'ðŸ"': '🔒',  # Lock
            'ðŸ'¤': '👤',  # User
            'ðŸ"œ': '📜',  # Scroll
            'ðŸ"': '📁',  # Folder
            'ðŸ"š': '📚',  # Books
            'ðŸ"Š': '📊',  # Chart
            'ðŸ"': '📝',  # Memo
            'ðŸ"°': '📰',  # Newspaper
            'ðŸ'¬': '💬',  # Speech
            'ðŸ"¢': '📢',  # Megaphone
            'ðŸŽ‰': '🎉',  # Party
            'âœ…': '✅',  # Check mark
            'âœ"': '✔️',  # Check
            'â„¹': 'ℹ️',  # Info
            'âš ': '⚠️',  # Warning
            'â­': '⭐',  # Star
            # Unicode escape sequences
            '\\u0027': "'",
            '\\u0026': '&',
        }
        
        fix_count = 0
        for old, new in replacements.items():
            count = content.count(old)
            if count > 0:
                content = content.replace(old, new)
                fix_count += count
                print(f"  ✓ Fixed {count} instances of {repr(old)} -> {new}")
        
        print(f"\n💾 Saving fixed file...")
        
        # Save with UTF-8 encoding
        with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(content)
        
        print(f"\n✅ SUCCESS! Fixed {fix_count} encoding issues")
        print(f"\n🎉 The Postman collection is now ready to import.")
        
        return True
        
    except Exception as e:
        print(f"\n❌ ERROR: {str(e)}")
        return False

if __name__ == "__main__":
    file_path = "ABC-Interview-VERIFIED-Complete.postman_collection.backup.json"
    success = fix_encoding(file_path)
    sys.exit(0 if success else 1)
