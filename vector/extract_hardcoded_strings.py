import os
import re
import xml.etree.ElementTree as ET

# Cấu hình
layout_dir = 'app/src/main/res/layout'
strings_dir = 'app/src/main/res/values'
target_langs = ['vi', 'lo']

# File đầu ra
strings_file = os.path.join(strings_dir, 'strings.xml')
template_tree = ET.ElementTree(ET.Element('resources'))
generated_keys = {}

# Regex tìm hardcoded text trong XML
attribute_pattern = re.compile(r'android:(text|hint|label)="([^@"][^"]+)"')

def generate_key(text):
    return 'auto_' + re.sub(r'\W+', '_', text.strip()).lower()

def extract_from_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    modified = False

    def replace(match):
        nonlocal modified
        attr, text = match.groups()
        key = generate_key(text)
        generated_keys[key] = text
        modified = True
        return f'android:{attr}="@string/{key}"'

    new_content = attribute_pattern.sub(replace, content)

    if modified:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)

def write_strings_file(path, content_map, translate=False):
    root = ET.Element('resources')
    for key, value in content_map.items():
        elem = ET.SubElement(root, 'string')
        elem.set('name', key)
        elem.text = value if not translate else f'Translate: {value}'
    tree = ET.ElementTree(root)
    ET.indent(tree, space="    ")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    tree.write(path, encoding='utf-8', xml_declaration=True)

# Thực thi
print("🔍 Scanning layout files for hardcoded strings...")
for filename in os.listdir(layout_dir):
    if filename.endswith('.xml'):
        extract_from_file(os.path.join(layout_dir, filename))

if generated_keys:
    print(f"✅ Found {len(generated_keys)} hardcoded strings. Writing to strings.xml...")
    write_strings_file(strings_file, generated_keys)

    for lang in target_langs:
        lang_path = f'app/src/main/res/values-{lang}/strings.xml'
        print(f"🌐 Creating {lang_path}...")
        write_strings_file(lang_path, generated_keys, translate=True)
else:
    print("🎉 No hardcoded strings found.")
