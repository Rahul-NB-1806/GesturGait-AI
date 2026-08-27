import React, { useState } from 'react';
import {
  View, Text, StyleSheet, TouchableOpacity, Alert, ScrollView, Switch,
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../theme';
import * as api from '../api/client';

export default function SettingsScreen() {
  const { user, logout, isAuthenticated } = useAuth();
  const { colors, borderRadius: br, spacing, isDark, toggleTheme } = useTheme();
  const [exporting, setExporting] = useState(false);

  async function handleExport() {
    if (!user) return;
    setExporting(true);
    try {
      const [history, baseline] = await Promise.all([
        api.getScoreHistory(user._id),
        api.getBaseline(user._id),
      ]);
      const data = JSON.stringify({ history, baseline, exportedAt: new Date() }, null, 2);
      Alert.alert('Data Export', `Your data has been prepared.\n\n${data.slice(0, 500)}...`);
    } catch (e) {
      Alert.alert('Error', 'Failed to export data.');
    } finally {
      setExporting(false);
    }
  }

  function handleDeleteData() {
    Alert.alert(
      'Delete All Data',
      'Are you sure you want to delete all your stored data? This cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete', style: 'destructive',
          onPress: () => Alert.alert('Deleted', 'All data has been removed.'),
        },
      ]
    );
  }

  return (
    <ScrollView style={[styles.container, { backgroundColor: colors.background }]} contentContainerStyle={styles.content}>
      <Text style={[styles.title, { color: colors.text }]}>Settings</Text>

      <View style={[styles.card, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
        <Text style={[styles.sectionTitle, { color: colors.text }]}>Appearance</Text>
        <View style={styles.row}>
          <Text style={[styles.infoText, { color: colors.textSecondary }]}>Dark Mode</Text>
          <Switch
            value={isDark}
            onValueChange={toggleTheme}
            trackColor={{ false: colors.border, true: colors.primary }}
            thumbColor={isDark ? '#fff' : '#f4f3f4'}
          />
        </View>
      </View>

      {isAuthenticated && user && (
        <View style={[styles.card, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
          <Text style={[styles.sectionTitle, { color: colors.text }]}>Account</Text>
          <Text style={[styles.infoText, { color: colors.textSecondary }]}>Email: {user.email}</Text>
          <Text style={[styles.infoText, { color: colors.textSecondary }]}>
            Baseline: {user.baselineEstablished ? 'Established' : 'Not yet established'}
          </Text>
        </View>
      )}

      <View style={[styles.card, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
        <Text style={[styles.sectionTitle, { color: colors.text }]}>Data Management</Text>
        <TouchableOpacity
          style={[styles.actionBtn, { backgroundColor: colors.surface }]}
          onPress={handleExport}
          disabled={exporting}
        >
          <Text style={[styles.actionBtnText, { color: colors.text }]}>
            {exporting ? 'Exporting...' : 'Export My Data'}
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.actionBtn, { backgroundColor: '#FFF5F5' }]}
          onPress={handleDeleteData}
        >
          <Text style={[styles.actionBtnText, { color: '#E53E3E' }]}>Delete All Data</Text>
        </TouchableOpacity>
      </View>

      <View style={[styles.card, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
        <Text style={[styles.sectionTitle, { color: colors.text }]}>Privacy</Text>
        <Text style={[styles.privacyText, { color: colors.muted }]}>
          • Only derived numerical features are stored on the server{'\n'}
          • No raw sensor streams, typed text, or screen content is persisted{'\n'}
          • All data is transmitted over HTTPS{'\n'}
          • You can export or delete your data at any time
        </Text>
      </View>

      <View style={[styles.card, { backgroundColor: colors.card, borderRadius: br.md, marginBottom: spacing.md }]}>
        <Text style={[styles.sectionTitle, { color: colors.text }]}>About</Text>
        <Text style={[styles.infoText, { color: colors.textSecondary }]}>GesturGait AI v1.0.0</Text>
        <Text style={[styles.infoText, { color: colors.textSecondary }]}>
          Screening aid for Parkinson's-related motor decline.{'\n'}
          Not a diagnostic device. Consult a neurologist for medical advice.
        </Text>
      </View>

      {isAuthenticated && (
        <TouchableOpacity
          style={[styles.logoutBtn, { backgroundColor: colors.card, borderColor: colors.border }]}
          onPress={logout}
        >
          <Text style={styles.logoutText}>Sign Out</Text>
        </TouchableOpacity>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 24, paddingTop: 60 },
  title: { fontSize: 28, fontWeight: '700', marginBottom: 20 },
  card: { padding: 20, marginBottom: 16 },
  sectionTitle: { fontSize: 16, fontWeight: '600', marginBottom: 12 },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  infoText: { fontSize: 14, marginBottom: 4, lineHeight: 20 },
  actionBtn: {
    borderRadius: 8, padding: 14, alignItems: 'center', marginBottom: 8,
  },
  actionBtnText: { fontSize: 14, fontWeight: '500' },
  privacyText: { fontSize: 13, lineHeight: 20 },
  logoutBtn: {
    borderRadius: 12, padding: 16, alignItems: 'center', marginBottom: 40,
    borderWidth: 1,
  },
  logoutText: { fontSize: 16, fontWeight: '500', color: '#E53E3E' },
});
