#!/usr/bin/env python3
"""
Script to update Postman collections to use gateway URL instead of direct service URLs.
"""
import json
import sys
import re


def update_postman_collection(file_path):
    """Update a Postman collection file to use gateway URL."""
    print(f"Processing {file_path}...")
    
    # Read the file
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Count replacements
    replacements = {
        'exam_service_url': 0,
        'question_service_url': 0,
        'user_service_url': 0,
        'auth_service_url': 0,
        'career_service_url': 0,
        'news_service_url': 0,
        'social_service_url': 0
    }
    
    # Replace service-specific URLs with gateway URL
    # Pattern 1: {{exam_service_url}}/exams -> {{gateway_url}}/exams
    patterns = [
        (r'\{\{exam_service_url\}\}/exams', '{{gateway_url}}/exams', 'exam_service_url'),
        (r'\{\{question_service_url\}\}/questions', '{{gateway_url}}/questions', 'question_service_url'),
        (r'\{\{user_service_url\}\}/users', '{{gateway_url}}/users', 'user_service_url'),
        (r'\{\{auth_service_url\}\}/auth', '{{gateway_url}}/auth', 'auth_service_url'),
        (r'\{\{career_service_url\}\}/career', '{{gateway_url}}/career', 'career_service_url'),
        (r'\{\{news_service_url\}\}/news', '{{gateway_url}}/news', 'news_service_url'),
        (r'\{\{social_service_url\}\}/posts', '{{gateway_url}}/posts', 'social_service_url'),
        (r'\{\{social_service_url\}\}/comments', '{{gateway_url}}/comments', 'social_service_url'),
    ]
    
    for pattern, replacement, key in patterns:
        count = len(re.findall(pattern, content))
        replacements[key] += count
        content = re.sub(pattern, replacement, content)
    
    # Replace variable references in host arrays
    # Pattern: "host": ["{{exam_service_url}}"] -> "host": ["{{gateway_url}}"]
    host_patterns = [
        (r'"host":\s*\[\s*"\{\{exam_service_url\}\}"\s*\]', '"host": ["{{gateway_url}}"]', 'exam_service_url'),
        (r'"host":\s*\[\s*"\{\{question_service_url\}\}"\s*\]', '"host": ["{{gateway_url}}"]', 'question_service_url'),
        (r'"host":\s*\[\s*"\{\{user_service_url\}\}"\s*\]', '"host": ["{{gateway_url}}"]', 'user_service_url'),
        (r'"host":\s*\[\s*"\{\{auth_service_url\}\}"\s*\]', '"host": ["{{gateway_url}}"]', 'auth_service_url'),
        (r'"host":\s*\[\s*"\{\{career_service_url\}\}"\s*\]', '"host": ["{{gateway_url}}"]', 'career_service_url'),
        (r'"host":\s*\[\s*"\{\{news_service_url\}\}"\s*\]', '"host": ["{{gateway_url}}"]', 'news_service_url'),
        (r'"host":\s*\[\s*"\{\{social_service_url\}\}"\s*\]', '"host": ["{{gateway_url}}"]', 'social_service_url'),
    ]
    
    for pattern, replacement, key in host_patterns:
        count = len(re.findall(pattern, content))
        replacements[key] += count
        content = re.sub(pattern, replacement, content)
    
    # Try to parse as JSON to update variables section
    try:
        data = json.loads(content)
        
        # Update or add gateway_url variable
        if 'variable' in data:
            # Remove old service URL variables
            data['variable'] = [v for v in data['variable'] 
                               if v.get('key') not in ['exam_service_url', 'question_service_url', 
                                                       'user_service_url', 'auth_service_url',
                                                       'career_service_url', 'news_service_url',
                                                       'social_service_url']]
            
            # Add gateway_url if not exists
            if not any(v.get('key') == 'gateway_url' for v in data['variable']):
                data['variable'].append({
                    "key": "gateway_url",
                    "value": "http://localhost:8080",
                    "type": "string"
                })
        
        # Write back as JSON
        content = json.dumps(data, indent=4, ensure_ascii=False)
        
    except json.JSONDecodeError:
        print(f"Warning: Could not parse {file_path} as JSON, using regex replacements only")
    
    # Write the updated content
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    # Print summary
    print(f"\nReplacements made in {file_path}:")
    total = 0
    for key, count in replacements.items():
        if count > 0:
            print(f"  - {key}: {count} occurrences")
            total += count
    print(f"Total replacements: {total}\n")
    
    return total


def main():
    """Main function to update both Postman collection files."""
    files = [
        'ABC-Interview-VERIFIED-Complete.postman_collection.backup.json',
        'postman-collections/ABC-Interview-Verified-Complete.postman_collection.json'
    ]
    
    total_replacements = 0
    for file_path in files:
        try:
            count = update_postman_collection(file_path)
            total_replacements += count
        except FileNotFoundError:
            print(f"Warning: File not found: {file_path}")
        except Exception as e:
            print(f"Error processing {file_path}: {str(e)}")
    
    print(f"\n✅ Update complete! Total replacements across all files: {total_replacements}")
    return 0


if __name__ == '__main__':
    sys.exit(main())
